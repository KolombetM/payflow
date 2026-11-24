package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.account.Account;
import dev.kolombetm.payflow.account.AccountService;
import dev.kolombetm.payflow.account.CreateAccountRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
public class ConcurrentTransferTest {

    @Autowired
    private AccountService accountService;
    @Autowired
    private PaymentService paymentService;

    @Test
    void twoConcurrentTransfersCannotOverdraw() throws Exception {
        Account fromAccount = accountService.createAccount(new CreateAccountRequest("Bob", Currency.getInstance("EUR")));
        Account toAccount = accountService.createAccount(new CreateAccountRequest("John", Currency.getInstance("EUR")));
        paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("100")), UUID.randomUUID().toString());
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);

        Future<String> first = pool.submit(transfer(start, fromAccount.getId(), toAccount.getId(), new BigDecimal("60")));
        Future<String> second = pool.submit(transfer(start, fromAccount.getId(), toAccount.getId(), new BigDecimal("60")));
        start.countDown();

        System.out.println(first.get() + " / " + second.get());
        pool.shutdown();

        assertThat(accountService.getById(fromAccount.getId()).getBalance()).isEqualByComparingTo(new BigDecimal("40"));
        assertThat(accountService.getById(toAccount.getId()).getBalance()).isEqualByComparingTo(new BigDecimal("60"));

        List<String> results = List.of(first.get(), second.get());
        System.out.println(results);

        assertThat(results).containsOnlyOnce("OK");
    }

    @Test
    void twoConcurrentTransfersDeadLock() throws Exception {
        Account fromAccount = accountService.createAccount(new CreateAccountRequest("Bob", Currency.getInstance("EUR")));
        Account toAccount = accountService.createAccount(new CreateAccountRequest("John", Currency.getInstance("EUR")));
        paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("100")), UUID.randomUUID().toString());
        paymentService.createDeposit(new CreateDepositRequest(toAccount.getId(), new BigDecimal("100")), UUID.randomUUID().toString());
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);

        Future<String> first = pool.submit(transfer(start, fromAccount.getId(), toAccount.getId(), new BigDecimal("10")));
        Future<String> second = pool.submit(transfer(start, toAccount.getId(), fromAccount.getId(), new BigDecimal("10")));
        start.countDown();

        System.out.println(first.get() + " / " + second.get());
        pool.shutdown();

        assertThat(accountService.getById(fromAccount.getId()).getBalance()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(accountService.getById(toAccount.getId()).getBalance()).isEqualByComparingTo(new BigDecimal("100"));

        List<String> results = List.of(first.get(), second.get());
        System.out.println(results);

        assertThat(results).containsOnly("OK");
    }

    private Callable<String> transfer(CountDownLatch start, UUID from, UUID to, BigDecimal amount) {
        return () -> {
            start.await();
            try {
                paymentService.createPayment(new CreatePaymentRequest(from, to, amount), UUID.randomUUID().toString());
                return "OK";
            } catch (Exception e) {
                return e.getClass().getSimpleName() + ": " + e.getMessage();
            }
        };
    }
}
