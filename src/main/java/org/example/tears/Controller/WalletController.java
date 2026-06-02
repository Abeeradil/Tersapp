package org.example.tears.Controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.WalletResponseDto;
import org.example.tears.DTO.WalletTransactionResponseDto;
import org.example.tears.Model.User;
import org.example.tears.Model.Wallet;
import org.example.tears.Service.AuthService;
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
}