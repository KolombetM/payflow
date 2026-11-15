package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.account.Account;
import dev.kolombetm.payflow.account.AccountService;
import dev.kolombetm.payflow.account.CreateAccountRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

public class PaymentServiceDailyLimitTest {

    private final BigDecimal DAILY_LIMIT = new BigDecimal("2500");
    private final BigDecimal PER_TRANSACTION_LIMIT = new BigDecimal("1000");
    private final Currency DEFAULT_CURRENCY = Currency.getInstance("EUR");

    private final AccountService accountService = new AccountService();
    private final PaymentLimitsProperties limits = new PaymentLimitsProperties(
            DAILY_LIMIT, PER_TRANSACTION_LIMIT, DEFAULT_CURRENCY);


    @Test
    void paymentUpToExactlyTheDailyLimitIsAllowed() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-14T10:00:00Z"), ZoneOffset.UTC);
        PaymentService paymentService = new PaymentService(accountService, clock, limits);

        CreateAccountRequest fromAccountRequest = new CreateAccountRequest("Bob", Currency.getInstance("EUR"));
        CreateAccountRequest toAccountRequest = new CreateAccountRequest("Rick", Currency.getInstance("EUR"));
        Account fromAccount = accountService.createAccount(fromAccountRequest);
        Account toAccount = accountService.createAccount(toAccountRequest);
        paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("5000")));

        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        Payment payment = paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("500")));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void paymentOverTheDailyLimitIsDeclined() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-14T10:00:00Z"), ZoneOffset.UTC);
        PaymentService paymentService = new PaymentService(accountService, clock, limits);

        CreateAccountRequest fromAccountRequest = new CreateAccountRequest("Bob", Currency.getInstance("EUR"));
        CreateAccountRequest toAccountRequest = new CreateAccountRequest("Rick", Currency.getInstance("EUR"));
        Account fromAccount = accountService.createAccount(fromAccountRequest);
        Account toAccount = accountService.createAccount(toAccountRequest);
        paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("5000")));

        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        assertThatThrownBy(() -> {
            paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        })
        .isInstanceOf(DailyLimitExceededException.class);
    }

    @Test
    void limitResetsAtMidnightUtc() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-14T23:59:59Z"), ZoneOffset.UTC);
        PaymentService paymentService = new PaymentService(accountService, clock, limits);

        CreateAccountRequest fromAccountRequest = new CreateAccountRequest("Bob", Currency.getInstance("EUR"));
        CreateAccountRequest toAccountRequest = new CreateAccountRequest("Rick", Currency.getInstance("EUR"));
        Account fromAccount = accountService.createAccount(fromAccountRequest);
        Account toAccount = accountService.createAccount(toAccountRequest);
        paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("5000")));

        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));

        clock.setInstant(Instant.parse("2026-11-15T00:00:00Z"));
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
    }

    @Test
    void paymentMadeExactlyAtMidnightCountsTowardsThatDay() {
        // часы: 2026-11-15T00:00:00Z
        MutableClock clock = new MutableClock(Instant.parse("2026-11-15T00:00:00Z"), ZoneOffset.UTC);
        PaymentService paymentService = new PaymentService(accountService, clock, limits);

        CreateAccountRequest fromAccountRequest = new CreateAccountRequest("Bob", Currency.getInstance("EUR"));
        CreateAccountRequest toAccountRequest = new CreateAccountRequest("Rick", Currency.getInstance("EUR"));
        Account fromAccount = accountService.createAccount(fromAccountRequest);
        Account toAccount = accountService.createAccount(toAccountRequest);
        paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("5000")));
        // два перевода по 1000 ровно в полночь
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));

        // часы: 2026-11-15T12:00:00Z
        clock.setInstant(Instant.parse("2026-11-15T12:00:00Z"));
        // перевод на 1000 — должен быть отклонён (1000 + 1000 + 1000 > 2500)
        assertThatThrownBy(() -> {
            paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        })
        .isInstanceOf(DailyLimitExceededException.class);
    }

}
