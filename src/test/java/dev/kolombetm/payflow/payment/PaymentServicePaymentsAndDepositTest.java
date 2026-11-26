package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.account.Account;
import dev.kolombetm.payflow.account.AccountService;
import dev.kolombetm.payflow.account.CreateAccountRequest;
import dev.kolombetm.payflow.idempotency.IdempotencyKeyMismatchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import dev.kolombetm.payflow.TestcontainersConfiguration;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public class PaymentServicePaymentsAndDepositTest {
    @Autowired
    private AccountService accountService;
    @Autowired
    private PaymentService paymentService;

    private Account externalAccount;
    private Account toAccount;
    private Account fromAccount;


    @BeforeEach
    void setUp() {
        externalAccount = accountService.getById(accountService.getExternalAccountId(Currency.getInstance("EUR")));
        fromAccount = accountService.createAccount(new CreateAccountRequest("Bob", Currency.getInstance("EUR")));
        toAccount = accountService.createAccount(new CreateAccountRequest("Rick", Currency.getInstance("EUR")));
    }

    @Test
    void depositToExternalAccount() {
        assertThatThrownBy(() ->
            paymentService.createDeposit(new CreateDepositRequest(externalAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString())
        ).isInstanceOf(SystemAccountNotAllowedException.class);
    }

    @Test
    void depositToAccount() {
        paymentService.createDeposit(new CreateDepositRequest(toAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString());
        Account externalAccountAfter = accountService.getById(accountService.getExternalAccountId(Currency.getInstance("EUR")));
        Account toAccountAfter = accountService.getById(toAccount.getId());

        assertThat(toAccountAfter.getBalance()).isEqualByComparingTo("1000");
        assertThat(externalAccountAfter.getBalance()).isEqualByComparingTo(externalAccount.getBalance().subtract(new BigDecimal("1000")));

    }

    @Test
    void withdrawToExternalAccount() {
        assertThatThrownBy(() ->
            paymentService.createPayment(new CreatePaymentRequest(toAccount.getId(), externalAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString())
        ).isInstanceOf(SystemAccountNotAllowedException.class);
    }

    @Test
    void withdrawFromExternalAccount() {
        assertThatThrownBy(() ->
                paymentService.createPayment(new CreatePaymentRequest(externalAccount.getId(), toAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString())
        ).isInstanceOf(SystemAccountNotAllowedException.class);
        Account toAccountAfter = accountService.getById(toAccount.getId());
        assertThat(toAccountAfter.getBalance()).isEqualByComparingTo(toAccount.getBalance());
    }

    @Test
    void twoCallsWithTheSameIdempotencyKeyAndSameCreatePaymentRequest() {
        paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString());
        String key = UUID.randomUUID().toString();
        Payment payment1 = paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("100")), key);
        Payment payment2 = paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("100")), key);
        assertThat(payment1.getId()).isEqualTo(payment2.getId());
    }

    @Test
    void twoCallsWithTheSameIdempotencyKeyButDifferentPaymentRequest() {
        paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("1000")), UUID.randomUUID().toString());
        String key = UUID.randomUUID().toString();
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("100")), key);
        assertThatThrownBy(() ->
                paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("50")), key)
        ).isInstanceOf(IdempotencyKeyMismatchException.class);
    }

    @Test
    void twoDepositsWithTheSameIdempotencyKeyAndTheSameDepositData() {
        String key = UUID.randomUUID().toString();
        Payment payment1 = paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("1000")), key);
        Payment payment2 = paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("1000")), key);
        assertThat(payment1.getId()).isEqualTo(payment2.getId());
    }

    @Test
    void twoDepositsWithTheSameIdempotencyKeyAndDifferentDepositData() {
        String key = UUID.randomUUID().toString();
        paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("1000")), key);
        assertThatThrownBy(() ->
                paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("100")), key)
        ).isInstanceOf(IdempotencyKeyMismatchException.class);
    }
}
