package dev.kolombetm.payflow.payment;

import dev.kolombetm.payflow.account.AccountResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest createPaymentRequest) {
        Payment payment = paymentService.createPayment(createPaymentRequest);
        return ResponseEntity
                .created(URI.create("/payments/" + payment.getId()))
                .body(PaymentResponse.from(payment));
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable UUID id) {
        return PaymentResponse.from(this.paymentService.getById(id));
    }

    @PostMapping("/deposits")
    public PaymentResponse deposit(@Valid @RequestBody CreateDepositRequest request) {
        return PaymentResponse.from(this.paymentService.createDeposit(request));

    }

}
