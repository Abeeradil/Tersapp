package org.example.tears.Service;

import lombok.AllArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.Enums.PaymentMethod;
import org.example.tears.Enums.TransactionType;
import org.example.tears.Model.User;
import org.example.tears.Model.Wallet;
import org.example.tears.Model.WalletTransaction;
import org.example.tears.Repository.WalletRepository;
import org.example.tears.Repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Service
@AllArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public Wallet getOrCreate(User user) {
        return walletRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Wallet w = new Wallet();
                    w.setUser(user);
                    w.setBalance(0);
                    w.setCreatedAt(LocalDateTime.now());
                    return walletRepository.save(w);
                });
    }

    public Wallet getMyWallet(User user) {
        return getOrCreate(user);
    }


    public List<WalletTransaction> getMyTransactions(User user) {
        Wallet wallet = getOrCreate(user);
        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
    }

    public void payFromWallet(User user, Integer amount, String ref) {
        Wallet wallet = getOrCreate(user);

        if (amount == null || amount <= 0) {
            throw new ApiException("المبلغ غير صحيح");
        }

        if (wallet.getBalance() == null || wallet.getBalance() < amount) {
            throw new ApiException("رصيد المحفظة غير كافي");
        }

        wallet.setBalance(wallet.getBalance() - amount);
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setAmount(amount);
        tx.setType(TransactionType.PAYMENT);
        tx.setPaymentMethod(PaymentMethod.WALLET);
        tx.setReferenceNumber(ref);
        tx.setDescription("Payment for request");
        tx.setCreatedAt(LocalDateTime.now());

        walletTransactionRepository.save(tx);
    }

    public void deposit(User user, Integer amount) {
        Wallet wallet = getOrCreate(user);

        if (amount == null || amount <= 0) {
            throw new ApiException("المبلغ غير صحيح");
        }

        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setAmount(amount);
        tx.setType(TransactionType.DEPOSIT);
        tx.setPaymentMethod(PaymentMethod.WALLET);
        tx.setReferenceNumber(UUID.randomUUID().toString());
        tx.setDescription("Wallet deposit");
        tx.setCreatedAt(LocalDateTime.now());

        walletTransactionRepository.save(tx);
    }
}