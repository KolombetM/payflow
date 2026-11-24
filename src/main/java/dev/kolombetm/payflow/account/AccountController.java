package dev.kolombetm.payflow.account;

import dev.kolombetm.payflow.common.PageResponse;
import dev.kolombetm.payflow.payment.Payment;
import dev.kolombetm.payflow.payment.PaymentRepository;
import dev.kolombetm.payflow.payment.PaymentResponse;
import dev.kolombetm.payflow.payment.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;
    private final PaymentService paymentService;

    public AccountController(AccountService accountService, PaymentService paymentService) {
        this.accountService = accountService;
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(request);
        return ResponseEntity
                .created(URI.create("/accounts/" + account.getId()))
                .body(AccountResponse.from(account));
    }

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable UUID id) {
        return AccountResponse.from(accountService.getById(id));
    }

    @GetMapping("/{id}/payments")
    public PageResponse<PaymentResponse> getPayments(@PathVariable UUID id, @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Payment> payments = paymentService.findByAccountId(id, pageable);
        return PageResponse.from(payments.map(PaymentResponse::from));
    }
}
