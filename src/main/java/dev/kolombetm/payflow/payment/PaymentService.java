package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.account.Account;
import dev.kolombetm.payflow.account.AccountService;
import org.springframework.stereotype.Service;

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

    public PaymentService(AccountService accountService, Clock clock, PaymentLimitsProperties paymentLimitsProperties, PaymentRepository paymentRepository) {
        this.accountService = accountService;
        this.clock = clock;
        this.paymentLimitsProperties = paymentLimitsProperties;
        this.paymentRepository = paymentRepository;
    }

    public Payment createPayment(CreatePaymentRequest request) {
        Account fromAccount = this.accountService.getById(request.fromAccountId());
        Account toAccount = this.accountService.getById(request.toAccountId());

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
        if (request.amount().add(spentToday).compareTo(this.paymentLimitsProperties.daily()) > 0) {
            throw new DailyLimitExceededException(fromAccount.getId(), fromAccount.getCurrency(), spentToday, request.amount(), paymentLimitsProperties.daily());
        }
        fromAccount = accountService.withdraw(fromAccount.getId(), request.amount());
        toAccount = accountService.deposit(toAccount.getId(), request.amount());
        Payment payment = Payment.transfer(fromAccount.getId(), toAccount.getId(), request.amount(), Instant.now(clock));

        return paymentRepository.save(payment);
    }

    public Payment createDeposit(CreateDepositRequest request) {
        Account depositAccount = accountService.deposit(request.accountId(), request.amount());
        Payment payment = Payment.deposit(depositAccount.getId(), request.amount(), Instant.now(clock));

        return paymentRepository.save(payment);
    }

    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

    public Payment getById(UUID id) {
        return findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
    }

    private BigDecimal getSpentToday(UUID accountId) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        Instant startOfDay = LocalDate.now(clock).atStartOfDay(clock.getZone()).toInstant();
        for (Payment payment : paymentRepository.findAll()) {
            if (payment.getFromAccountId() != null
                && payment.getFromAccountId().equals(accountId)
                && payment.getType().equals(PaymentType.TRANSFER)
                && payment.getStatus().equals(PaymentStatus.COMPLETED)
                && !payment.getCreatedAt().isBefore(startOfDay)) {
                totalAmount = totalAmount.add(payment.getAmount());
            }
        }
        return totalAmount;
    }
}
