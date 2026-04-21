package com.nexos.bankinc.service;

import com.nexos.bankinc.dto.request.AnulationRequest;
import com.nexos.bankinc.dto.request.BuyRequest;
import com.nexos.bankinc.dto.response.TransactionResponse;
import com.nexos.bankinc.entity.Card;
import com.nexos.bankinc.entity.Transaction;
import com.nexos.bankinc.entity.TransactionStatus;
import com.nexos.bankinc.exception.*;
import com.nexos.bankinc.repository.CardRepository;
import com.nexos.bankinc.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  CardRepository cardRepository) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
    }

    @Override
    public TransactionResponse buy(BuyRequest request) {
        Card card = findCardOrThrow(request.getCardId());

        // Validaciones
        if (!card.isActive()) throw new CardNotActiveException();
        if (card.isBlocked()) throw new CardBlockedException();
        if (card.getExpirationDate().isBefore(LocalDate.now())) throw new CardExpiredException();
        if (card.getBalance().compareTo(request.getPrice()) < 0) throw new InsufficientBalanceException();
        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) throw new InvalidAmountException();

        // Descontar saldo
        card.setBalance(card.getBalance().subtract(request.getPrice()));
        cardRepository.save(card);

        // Guardar transacción
        Transaction transaction = Transaction.builder()
                .card(card)
                .price(request.getPrice())
                .build();
        Transaction saved = transactionRepository.save(transaction);

        return mapToResponse(saved);
    }

    @Override
    public TransactionResponse getTransaction(Long transactionId) {
        Transaction transaction = findTransactionOrThrow(transactionId);
        return mapToResponse(transaction);
    }

    @Override
    public TransactionResponse annulTransaction(AnulationRequest request) {
        Transaction transaction = findTransactionOrThrow(request.getTransactionId());

        // Validaciones
        if (transaction.getStatus() == TransactionStatus.ANULLED)
            throw new TransactionAlreadyAnnulledException();

        if (transaction.getTransactionDate().isBefore(LocalDateTime.now().minusHours(24)))
            throw new TransactionExpiredException();

        // Devolver saldo
        Card card = transaction.getCard();
        card.setBalance(card.getBalance().add(transaction.getPrice()));
        cardRepository.save(card);

        // Marcar como anulada
        transaction.setStatus(TransactionStatus.ANULLED);
        Transaction saved = transactionRepository.save(transaction);

        return mapToResponse(saved);
    }

    @Override
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Card findCardOrThrow(String cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }

    private Transaction findTransactionOrThrow(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getCard().getCardId(),
                transaction.getPrice(),
                transaction.getStatus(),
                transaction.getTransactionDate()
        );
    }
}