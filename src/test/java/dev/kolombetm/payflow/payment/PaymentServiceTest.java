package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.account.Account;
import dev.kolombetm.payflow.account.AccountService;
import dev.kolombetm.payflow.account.CreateAccountRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
public class PaymentServiceTest {
    @Autowired
    private AccountService accountService;
    @Autowired
    private PaymentService paymentService;

    private Account externalAccount;
    private Account toAccount;


    @BeforeEach
    void setUp() {
        externalAccount = accountService.getById(accountService.getExternalAccountId(Currency.getInstance("EUR")));
        toAccount = accountService.createAccount(new CreateAccountRequest("Rick", Currency.getInstance("EUR")));
    }

    @Test
    void depositToExternalAccount() {
        assertThatThrownBy(() ->
            paymentService.createDeposit(new CreateDepositRequest(externalAccount.getId(), new BigDecimal("1000")))
        ).isInstanceOf(SystemAccountNotAllowedException.class);
    }

    @Test
    void depositToAccount() {
        paymentService.createDeposit(new CreateDepositRequest(toAccount.getId(), new BigDecimal("1000")));
        Account externalAccountAfter = accountService.getById(accountService.getExternalAccountId(Currency.getInstance("EUR")));
        Account toAccountAfter = accountService.getById(toAccount.getId());

        assertThat(toAccountAfter.getBalance()).isEqualByComparingTo("1000");
        assertThat(externalAccountAfter.getBalance()).isEqualByComparingTo(externalAccount.getBalance().subtract(new BigDecimal("1000")));

    }

    @Test
    void withdrawToExternalAccount() {
        assertThatThrownBy(() ->
            paymentService.createPayment(new CreatePaymentRequest(toAccount.getId(), externalAccount.getId(), new BigDecimal("1000")))
        ).isInstanceOf(SystemAccountNotAllowedException.class);
    }

    @Test
    void withdrawFromExternalAccount() {
        assertThatThrownBy(() ->
                paymentService.createPayment(new CreatePaymentRequest(externalAccount.getId(), toAccount.getId(), new BigDecimal("1000")))
        ).isInstanceOf(SystemAccountNotAllowedException.class);
        Account toAccountAfter = accountService.getById(toAccount.getId());
        assertThat(toAccountAfter.getBalance()).isEqualByComparingTo(toAccount.getBalance());
    }
}
