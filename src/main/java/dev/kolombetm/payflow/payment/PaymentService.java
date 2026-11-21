package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.account.Account;
import dev.kolombetm.payflow.account.AccountService;
import dev.kolombetm.payflow.ledger.LedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final AccountService accountService;
    private final Clock clock;
    private final PaymentLimitsProperties paymentLimitsProperties;
    private final PaymentRepository paymentRepository;
    private final LedgerService ledgerService;

    public PaymentService(AccountService accountService, Clock clock, PaymentLimitsProperties paymentLimitsProperties, PaymentRepository paymentRepository, LedgerService ledgerService) {
        this.accountService = accountService;
        this.clock = clock;
        this.paymentLimitsProperties = paymentLimitsProperties;
        this.paymentRepository = paymentRepository;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public Payment createPayment(CreatePaymentRequest request) {
        Account fromAccount;
        Account toAccount;
        if (request.fromAccountId().compareTo(request.toAccountId()) < 0) {
            fromAccount = this.accountService.getByIdForUpdate(request.fromAccountId());
            toAccount = this.accountService.getByIdForUpdate(request.toAccountId());
        } else {
            toAccount = this.accountService.getByIdForUpdate(request.toAccountId());
            fromAccount = this.accountService.getByIdForUpdate(request.fromAccountId());
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
        fromAccount = accountService.withdraw(fromAccount.getId(), request.amount());
        toAccount = accountService.deposit(toAccount.getId(), request.amount());
        Payment payment = Payment.transfer(fromAccount.getId(), toAccount.getId(), request.amount(), Instant.now(clock));

        payment = paymentRepository.save(payment);
        ledgerService.recordTransfers(payment);
        return payment;
    }

    @Transactional
    public Payment createDeposit(CreateDepositRequest request) {
        Account depositAccount = accountService.deposit(request.accountId(), request.amount());
        Payment payment = Payment.deposit(depositAccount.getId(), request.amount(), Instant.now(clock));

        return paymentRepository.save(payment);
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
}
