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

    public PaymentService(AccountService accountService, Clock clock) {
        this.accountService = accountService;
        this.clock = clock;
    }

    public Payment createPayment(CreatePaymentRequest request) {
        Account fromAccount = this.accountService.getById(request.fromAccountId());
        Account toAccount = this.accountService.getById(request.toAccountId());
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.amount()));
        toAccount.setBalance(toAccount.getBalance().add(request.amount()));
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setFromAccountId(fromAccount.getId());
        payment.setToAccountId(toAccount.getId());
        payment.setAmount(request.amount());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCreateAt(Instant.now(clock));

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
