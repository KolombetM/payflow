package dev.kolombetm.payflow.account;

import dev.kolombetm.payflow.payment.Payment;
import dev.kolombetm.payflow.payment.PaymentService;
import dev.kolombetm.payflow.payment.PaymentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccountController.class)
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AccountService accountService;
    @MockitoBean
    PaymentService paymentService;

    @Test
    void createAccountReturns201WithLocation() throws Exception {
        Account account = new Account("Bob", Currency.getInstance("EUR"));
        when(accountService.createAccount(any())).thenReturn(account);

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "ownerName": "Bob", "currency": "EUR"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/accounts/" + account.getId()))
                .andExpect(jsonPath("$.ownerName").value("Bob"))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void createAccountThrows400WithNoOwnerName() throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        { "ownerName": "", "currency": "EUR"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("ownerName"))
                .andExpect(jsonPath("$.detail").value("Request validation failed"));

        verify(accountService, never()).createAccount(any());
    }

    @Test
    void getAccountThrows404ForUnknownAccount() throws Exception {
        UUID accountId = UUID.randomUUID();
        when(accountService.getById(accountId)).thenThrow(new AccountNotFoundException(accountId));
        mockMvc.perform(get("/accounts/" + accountId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.detail").value("Account " + accountId + " not found"));
    }

    @Test
    void paymentsPageHasStableJsonFormat() throws Exception {
        UUID accountId = UUID.randomUUID();
        Payment payment = Payment.create(accountId, UUID.randomUUID(), new BigDecimal("10.00"),
                PaymentType.TRANSFER, Instant.parse("2026-11-27T10:00:00Z"));

        Page<Payment> page = new PageImpl<>(List.of(payment), PageRequest.of(1, 2), 5);
        when(paymentService.findByAccountId(eq(accountId), any())).thenReturn(page);

        mockMvc.perform(get("/accounts/{id}/payments", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(payment.getId().toString()))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.pageable").doesNotExist());
    }
}
