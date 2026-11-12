package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.account.Account;
import dev.kolombetm.payflow.account.AccountService;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {

    private final AccountService accountService;
    private final Clock clock;
    private final Map<UUID, Payment> payments = new ConcurrentHashMap<>();
    private final PaymentLimitsProperties paymentLimitsProperties;

    public PaymentService(AccountService accountService, Clock clock, PaymentLimitsProperties paymentLimitsProperties) {
        this.accountService = accountService;
        this.clock = clock;
        this.paymentLimitsProperties = paymentLimitsProperties;
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
        accountService.withdraw(fromAccount.getId(), request.amount());
        accountService.deposit(toAccount.getId(), request.amount());
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setFromAccountId(fromAccount.getId());
        payment.setToAccountId(toAccount.getId());
        payment.setAmount(request.amount());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentType(PaymentType.TRANSFER);
        payment.setCreatedAt(Instant.now(clock));

        payments.put(payment.getId(), payment);
        return payment;
    }

    public Payment createDeposit(CreateDepositRequest request) {
        accountService.deposit(request.accountId(), request.amount());
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setToAccountId(request.accountId());
        payment.setAmount(request.amount());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentType(PaymentType.DEPOSIT);
        payment.setCreatedAt(Instant.now(clock));

        payments.put(payment.getId(), payment);
        return payment;
    }

    public Optional<Payment> findById(UUID id) {
        return Optional.ofNullable(payments.get(id));
    }

    public Payment getById(UUID id) {
        return findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
    }
}
