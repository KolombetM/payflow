package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.account.InsufficientFundsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PaymentService paymentService;

    @Test
    void createPaymentReturns201WithLocation() throws Exception {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        Payment payment = Payment.create(from, to, new BigDecimal("50.00"), PaymentType.TRANSFER, Instant.now());
        when(paymentService.createPayment(any(), eq("key-1"))).thenReturn(payment);

        mockMvc.perform(post("/payments")
                        .header("Idempotency-Key", "key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "fromAccountId": "%s", "toAccountId": "%s", "amount": 50.00 }
                                """.formatted(from, to)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/payments/" + payment.getId()))
                .andExpect(jsonPath("$.amount").value(50.00));
    }

    @Test
    void createPaymentThrows400WithNoIdenpotencyKey() throws Exception {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        Payment payment = Payment.create(from, to, new BigDecimal("50.00"), PaymentType.TRANSFER, Instant.now());
        when(paymentService.createPayment(any(), any())).thenReturn(payment);

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                { "fromAccountId": "%s", "toAccountId": "%s", "amount": 50.00 }
                                """.formatted(from, to)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.detail").value("Required header 'Idempotency-Key' is not present."));
        verify(paymentService, never()).createPayment(any(), any());
    }

    @Test
    void createPaymentThrows400WithInvalidAmount() throws Exception {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        Payment payment = Payment.create(from, to, new BigDecimal("50.00"), PaymentType.TRANSFER, Instant.now());
        when(paymentService.createPayment(any(), any())).thenReturn(payment);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "key-1")
                        .content("""
                                { "fromAccountId": "%s", "toAccountId": "%s", "amount": -50.00 }
                                """.formatted(from, to)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.detail").value("Validation failure"));
        verify(paymentService, never()).createPayment(any(), any());
    }

    @Test
    void createPaymentThrows422WhenInsufficientFunds() throws Exception {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        when(paymentService.createPayment(any(), any())).thenThrow(InsufficientFundsException.class);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "key-1")
                        .content("""
                                { "fromAccountId": "%s", "toAccountId": "%s", "amount": 50.00 }
                                """.formatted(from, to)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void createPaymentThrows400WhenIdempotencyKeyIsTooLong() throws Exception {
        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        String key = "x".repeat(101);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", key)
                        .content("""
                                { "fromAccountId": "%s", "toAccountId": "%s", "amount": 50.00 }
                                """.formatted(from, to)))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }
}
