package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.account.Account;
import dev.kolombetm.payflow.account.AccountService;
import dev.kolombetm.payflow.account.CreateAccountRequest;
import dev.kolombetm.payflow.account.InsufficientFundsException;
import dev.kolombetm.payflow.ledger.ReconciliationReport;
import dev.kolombetm.payflow.ledger.ReconciliationService;
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
public class ReconciliationServiceTest {
    @Autowired
    private AccountService accountService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private ReconciliationService reconciliationService;

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        fromAccount = accountService.createAccount(new CreateAccountRequest("Max", Currency.getInstance("EUR")));
        toAccount = accountService.createAccount(new CreateAccountRequest("Rick", Currency.getInstance("EUR")));
    }

    @Test
    void deposit() {
        paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("1000")));
        ReconciliationReport report = reconciliationService.check();
        assertThat(report.isConsistent()).isTrue();
    }

    @Test
    void transfer() {
        paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("1000")));
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        assertThat(reconciliationService.check().isConsistent()).isTrue();
    }

    @Test
    void declinedTransfer() {
        paymentService.createDeposit(new CreateDepositRequest(fromAccount.getId(), new BigDecimal("1000")));
        paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        try {
            paymentService.createPayment(new CreatePaymentRequest(fromAccount.getId(), toAccount.getId(), new BigDecimal("1000")));
        } catch (InsufficientFundsException _) {}
        assertThat(reconciliationService.check().isConsistent()).isTrue();
    }
}
