package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.account.Account;
import dev.kolombetm.payflow.account.AccountService;
import dev.kolombetm.payflow.account.AccountType;
import dev.kolombetm.payflow.idempotency.IdempotencyKey;
import dev.kolombetm.payflow.idempotency.IdempotencyKeyMismatchException;
import dev.kolombetm.payflow.idempotency.IdempotencyRepository;
import dev.kolombetm.payflow.ledger.LedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final AccountService accountService;
    private final Clock clock;
    private final PaymentLimitsProperties paymentLimitsProperties;
    private final PaymentRepository paymentRepository;
    private final LedgerService ledgerService;
    private final IdempotencyRepository idempotencyRepository;

    public PaymentService(AccountService accountService, Clock clock, PaymentLimitsProperties paymentLimitsProperties, PaymentRepository paymentRepository, LedgerService ledgerService, IdempotencyRepository idempotencyRepository) {
        this.accountService = accountService;
        this.clock = clock;
        this.paymentLimitsProperties = paymentLimitsProperties;
        this.paymentRepository = paymentRepository;
        this.ledgerService = ledgerService;
        this.idempotencyRepository = idempotencyRepository;
    }

    @Transactional
    public Payment createPayment(CreatePaymentRequest request, String idempotencyKey) {
        LockedAccounts accounts = lockInOrder(request.fromAccountId(), request.toAccountId());

        Optional<Payment> existingPayment = findExistingPayment(
                idempotencyKey, request.fromAccountId(), request.toAccountId(), request.amount());
        if (existingPayment.isPresent()) {
            return existingPayment.get();
        }

        Account fromAccount = accounts.from();
        Account toAccount = accounts.to();

        if (toAccount.getType() != AccountType.CUSTOMER || fromAccount.getType() != AccountType.CUSTOMER) {
            throw new SystemAccountNotAllowedException();
        }
        
        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new SameAccountTransferException(fromAccount.getId());
        }
        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new DifferentCurrencyException(fromAccount.getId(), fromAccount.getCurrency(), toAccount.getId(), toAccount.getCurrency());
        }
        if (request.amount().compareTo(paymentLimitsProperties.perTransaction()) > 0) {
            throw new PaymentExceedsPerTransactionLimitException(fromAccount.getId(), toAccount.getId(), request.amount(), paymentLimitsProperties.perTransaction());
        }
        BigDecimal spentToday = getSpentToday(fromAccount.getId());
        if (request.amount().add(spentToday).compareTo(paymentLimitsProperties.daily()) > 0) {
            throw new DailyLimitExceededException(fromAccount.getId(), fromAccount.getCurrency(), spentToday, request.amount(), paymentLimitsProperties.daily());
        }
        return move(accounts, request.amount(), PaymentType.TRANSFER, idempotencyKey);
    }

    @Transactional
    public Payment createDeposit(CreateDepositRequest request, String idempotencyKey) {
        Currency accountCurrency = accountService.getCurrency(request.accountId());
        UUID externalAccountId = accountService.getExternalAccountId(accountCurrency);
        LockedAccounts accounts = lockInOrder(externalAccountId, request.accountId());

        Optional<Payment> existingPayment = findExistingPayment(
                idempotencyKey, externalAccountId, request.accountId(), request.amount());
        if (existingPayment.isPresent()) {
            return existingPayment.get();
        }

        if (accounts.from().getType() != AccountType.EXTERNAL || accounts.to().getType() != AccountType.CUSTOMER) {
            throw new SystemAccountNotAllowedException();
        }

        return move(accounts, request.amount(), PaymentType.DEPOSIT, idempotencyKey);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Payment getById(UUID id) {
        return findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
    }

    private BigDecimal getSpentToday(UUID accountId) {
        Instant startOfDay = LocalDate.now(clock).atStartOfDay(clock.getZone()).toInstant();
        return paymentRepository.sumSpentSince(accountId, PaymentType.TRANSFER, PaymentStatus.COMPLETED, startOfDay);
    }

    private record LockedAccounts(Account from, Account to) {}

    private LockedAccounts lockInOrder(UUID fromId, UUID toId) {
        boolean fromFirst = fromId.compareTo(toId) < 0;
        Account first  = accountService.getByIdForUpdate(fromFirst ? fromId : toId);   // меньший id
        Account second = accountService.getByIdForUpdate(fromFirst ? toId : fromId);   // больший id
        return fromFirst ? new LockedAccounts(first, second) : new LockedAccounts(second, first);
    }

    private Payment move(LockedAccounts accounts, BigDecimal amount, PaymentType type, String idempotencyKey) {
        accountService.withdraw(accounts.from().getId(), amount);
        accountService.deposit(accounts.to().getId(), amount);
        Instant now = Instant.now(clock).truncatedTo(ChronoUnit.MICROS);
        Payment payment = paymentRepository.save(
                Payment.create(accounts.from().getId(), accounts.to().getId(), amount.setScale(2), type, now));
        ledgerService.record(payment);
        idempotencyRepository.save(new IdempotencyKey(idempotencyKey, payment.getId(), now));
        return payment;
    }

    private Optional<Payment> findExistingPayment(String idempotencyKey, UUID fromId, UUID toId, BigDecimal amount) {
        return idempotencyRepository.findByKey(idempotencyKey)
                .map(key -> paymentRepository.findById(key.getPaymentId())
                        .orElseThrow(() -> new IllegalStateException("Idempotency key exists but payment does not")))
                .map(payment -> {
                    boolean sameRequest = payment.getFromAccountId().equals(fromId)
                            && payment.getToAccountId().equals(toId)
                            && payment.getAmount().compareTo(amount) == 0;
                    if (!sameRequest) {
                        throw new IdempotencyKeyMismatchException(fromId, toId, amount);
                    }
                    return payment;
                });
    }
}
