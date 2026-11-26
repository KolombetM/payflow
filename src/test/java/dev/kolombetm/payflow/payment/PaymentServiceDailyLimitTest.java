package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.account.Account;
import dev.kolombetm.payflow.account.AccountService;
import dev.kolombetm.payflow.account.CreateAccountRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = {"payflow.limits.daily=2500", "payflow.limits.per-transaction=1000", "payflow.limits.currency=EUR"})
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public class PaymentServiceDailyLimitTest {
    @Autowired
    private PaymentLimitsProperties limits;
    @Autowired
    private AccountService accountService;
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MutableClock clock;


    private Account fromAccount;
    private Account toAccount;

    @TestConfiguration
    static class TestClockConfig {
        @Bean
        @Primary
        public MutableClock mutableClock(){
            return new MutableClock(Instant.parse("2026-11-14T10:00:00Z"), ZoneOffset.UTC);
        }
    }


    @BeforeEach
    void setUp() {
        clock.setInstant(Instant.parse("2026-11-14T10:00:00Z"));
        fromAccount = accountService.createAccount(new CreateAccountRequest("Bob", limits.currency()));
        toAccount = accountService.createAccount(new CreateAccountRequest("Rick", limits.currency()));
        paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("5000")), UUID.randomUUID().toString());
    }


    @Test
    void paymentUpToExactlyTheDailyLimitIsAllowed() {
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString());
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString());
        Payment payment = paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("500")), UUID.randomUUID().toString());

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void paymentOverTheDailyLimitIsDeclined() {
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString());
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString());
        assertThatThrownBy(() ->
            paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString())
        )
        .isInstanceOf(DailyLimitExceededException.class);
    }

    @Test
    void limitResetsAtMidnightUtc() {
        clock.setInstant(Instant.parse("2026-11-14T23:59:59Z"));

        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString());
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString());

        clock.setInstant(Instant.parse("2026-11-15T00:00:00Z"));
        Payment payment = paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString());
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void paymentMadeExactlyAtMidnightCountsTowardsThatDay() {
        clock.setInstant(Instant.parse("2026-11-15T00:00:00Z"));

        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString());
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString());

        clock.setInstant(Instant.parse("2026-11-15T12:00:00Z"));
        assertThatThrownBy(() ->
            paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString())
        )
        .isInstanceOf(DailyLimitExceededException.class);
    }

}
