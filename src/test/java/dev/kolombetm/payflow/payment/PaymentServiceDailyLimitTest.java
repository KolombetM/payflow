package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.account.Account;
import dev.kolombetm.payflow.account.AccountService;
import dev.kolombetm.payflow.account.CreateAccountRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

public class PaymentServiceDailyLimitTest {

    private static final BigDecimal DAILY_LIMIT = new BigDecimal("2500");
    private static final BigDecimal PER_TRANSACTION_LIMIT = new BigDecimal("1000");
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("EUR");

    private final PaymentLimitsProperties limits = new PaymentLimitsProperties(
            DAILY_LIMIT, PER_TRANSACTION_LIMIT, DEFAULT_CURRENCY);

    private final AccountService accountService = new AccountService();
    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-14T10:00:00Z"), ZoneOffset.UTC);
    private final PaymentService paymentService = new PaymentService(accountService, clock, limits);

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        fromAccount = accountService.createAccount(new CreateAccountRequest("Bob", DEFAULT_CURRENCY));
        toAccount = accountService.createAccount(new CreateAccountRequest("Rick", DEFAULT_CURRENCY));
        paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("5000")));
    }


    @Test
    void paymentUpToExactlyTheDailyLimitIsAllowed() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-14T10:00:00Z"), ZoneOffset.UTC);
        PaymentService paymentService = new PaymentService(accountService, clock, limits);

        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        Payment payment = paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("500")));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void paymentOverTheDailyLimitIsDeclined() {
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        assertThatThrownBy(() ->
            paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")))
        )
        .isInstanceOf(DailyLimitExceededException.class);
    }

    @Test
    void limitResetsAtMidnightUtc() {
        clock.setInstant(Instant.parse("2026-11-14T23:59:59Z"));

        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));

        clock.setInstant(Instant.parse("2026-11-15T00:00:00Z"));
        Payment payment = paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void paymentMadeExactlyAtMidnightCountsTowardsThatDay() {
        clock.setInstant(Instant.parse("2026-11-15T00:00:00Z"));
        PaymentService paymentService = new PaymentService(accountService, clock, limits);

        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));

        clock.setInstant(Instant.parse("2026-11-15T12:00:00Z"));
        assertThatThrownBy(() ->
            paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")))
        )
        .isInstanceOf(DailyLimitExceededException.class);
    }

}
