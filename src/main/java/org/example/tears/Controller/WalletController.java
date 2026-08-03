package org.example.tears.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.*;
import org.example.tears.InpDTO.CreateRequestStepDto;
import org.example.tears.Model.User;
import org.example.tears.Model.Wallet;
import org.example.tears.OutDTO.RequestResponseDto;
import org.example.tears.Service.AuthService;
import org.example.tears.Service.PaymentIntentService;
import org.example.tears.Service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tears/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final AuthService authService;
    private final PaymentIntentService paymentIntentService;

    @GetMapping("/me")
    public ResponseEntity<WalletResponseDto> getMyWallet(HttpServletRequest request) {
        User user = authService.getAuthenticatedUser(request);

        Wallet wallet = walletService.getMyWallet(user);

        return ResponseEntity.ok(
                new WalletResponseDto(
                        wallet.getId(),
                        wallet.getBalance(),
                        wallet.getBalance() / 100.0
                )
        );
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<WalletTransactionResponseDto>> getMyTransactions(
            HttpServletRequest request
    ) {
        User user = authService.getAuthenticatedUser(request);

        List<WalletTransactionResponseDto> transactions =
                walletService.getMyTransactions(user)
                        .stream()
                        .map(tx -> new WalletTransactionResponseDto(
                                tx.getId(),
                                tx.getAmount(),
                                tx.getAmount() / 100.0,
                                tx.getType().name(),
                                tx.getPaymentMethod().name(),
                                tx.getReferenceNumber(),
                                tx.getDescription(),
                                tx.getCreatedAt()
                        ))
                        .toList();

        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/deposit/test")
    public ResponseEntity<WalletResponseDto> depositTest(
            HttpServletRequest request,
            @RequestParam Integer amount
    ) {
        User user = authService.getAuthenticatedUser(request);

        walletService.deposit(user, amount);

        Wallet wallet = walletService.getMyWallet(user);

        return ResponseEntity.ok(
                new WalletResponseDto(
                        wallet.getId(),
                        wallet.getBalance(),
                        wallet.getBalance() / 100.0
                )
        );
    }

    @PostMapping("/topup/mobile/prepare")
    public ResponseEntity<MobilePaymentResponse> prepareWalletTopup(

            HttpServletRequest request,

            @RequestBody @Valid WalletTopupRequest dto
    ) {

        return ResponseEntity.ok(

                walletService.prepareWalletTopUp(request, dto)

        );
    }

    @PostMapping("/topup/mobile/confirm")
    public ResponseEntity<WalletResponseDto> confirmWalletTopup(

            HttpServletRequest request,

            @RequestBody ConfirmMobilePaymentRequest dto
    ) {

        return ResponseEntity.ok(

                walletService.confirmWalletTopup(dto, request)

        );
    }

    @PostMapping("/wallet/initial")
    public ResponseEntity<RequestResponseDto> payInitialWithWallet(
            HttpServletRequest request,
            @RequestBody CreateRequestStepDto dto
    ) {

        return ResponseEntity.ok(
                paymentIntentService.payRequestWithWallet(request, dto)
        );
    }

    @PostMapping("/wallet/final/{requestId}")
    public ResponseEntity<RequestResponseDto> payFinalWithWallet(
            HttpServletRequest request,
            @PathVariable Integer requestId
    ) {

        return ResponseEntity.ok(
                paymentIntentService.payFinalWithWallet(requestId, request)
        );
    }
}