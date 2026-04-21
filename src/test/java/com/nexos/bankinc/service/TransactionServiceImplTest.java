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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Card testCard;
    private Transaction testTransaction;
    private BuyRequest validBuyRequest;
    private AnulationRequest validAnulationRequest;

    @BeforeEach
    void setUp() {
        testCard = Card.builder()
                .cardId("1234567890123456")
                .productId("123456")
                .clientName("Test User")
                .expirationDate(LocalDate.now().plusMonths(6))
                .balance(new BigDecimal("1000.00"))
                .active(true)
                .blocked(false)
                .build();

        testTransaction = Transaction.builder()
                .transactionId(1L)
                .card(testCard)
                .price(new BigDecimal("100.00"))
                .status(TransactionStatus.ACTIVE)
                .transactionDate(LocalDateTime.now())
                .build();

        validBuyRequest = new BuyRequest();
        validBuyRequest.setCardId("1234567890123456");
        validBuyRequest.setPrice(new BigDecimal("100.00"));

        validAnulationRequest = new AnulationRequest();
        validAnulationRequest.setTransactionId(1L);
    }

    @Test
    void buy_SuccessfulTransaction() {
        // Given
        when(cardRepository.findById(anyString())).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        TransactionResponse result = transactionService.buy(validBuyRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getTransactionId());
        assertEquals("1234567890123456", result.getCardId());
        assertEquals(new BigDecimal("100.00"), result.getPrice());
        assertEquals(TransactionStatus.ACTIVE, result.getStatus());

        verify(cardRepository).save(testCard);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void buy_CardNotFound_ThrowsCardNotFoundException() {
        // Given
        when(cardRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CardNotFoundException.class, () -> transactionService.buy(validBuyRequest));
        verify(cardRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void buy_CardNotActive_ThrowsCardNotActiveException() {
        // Given
        testCard.setActive(false);
        when(cardRepository.findById(anyString())).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(CardNotActiveException.class, () -> transactionService.buy(validBuyRequest));
        verify(cardRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void buy_CardBlocked_ThrowsCardBlockedException() {
        // Given
        testCard.setBlocked(true);
        when(cardRepository.findById(anyString())).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(CardBlockedException.class, () -> transactionService.buy(validBuyRequest));
        verify(cardRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void buy_CardExpired_ThrowsCardExpiredException() {
        // Given
        testCard.setExpirationDate(LocalDate.now().minusDays(1));
        when(cardRepository.findById(anyString())).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(CardExpiredException.class, () -> transactionService.buy(validBuyRequest));
        verify(cardRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void buy_InsufficientBalance_ThrowsInsufficientBalanceException() {
        // Given
        testCard.setBalance(new BigDecimal("50.00"));
        when(cardRepository.findById(anyString())).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(InsufficientBalanceException.class, () -> transactionService.buy(validBuyRequest));
        verify(cardRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void buy_InvalidAmount_ThrowsInvalidAmountException() {
        // Given
        validBuyRequest.setPrice(BigDecimal.ZERO);
        when(cardRepository.findById(anyString())).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(InvalidAmountException.class, () -> transactionService.buy(validBuyRequest));
        verify(cardRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getTransaction_Successful() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        // When
        TransactionResponse result = transactionService.getTransaction(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getTransactionId());
        assertEquals("1234567890123456", result.getCardId());
        verify(transactionRepository).findById(1L);
    }

    @Test
    void getTransaction_NotFound_ThrowsTransactionNotFoundException() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransaction(1L));
        verify(transactionRepository).findById(1L);
    }

    @Test
    void annulTransaction_Successful() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // When
        TransactionResponse result = transactionService.annulTransaction(validAnulationRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getTransactionId());
        verify(cardRepository).save(testCard);
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    void annulTransaction_TransactionNotFound_ThrowsTransactionNotFoundException() {
        // Given
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(TransactionNotFoundException.class, () -> transactionService.annulTransaction(validAnulationRequest));
        verify(cardRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void annulTransaction_AlreadyAnnulled_ThrowsTransactionAlreadyAnnulledException() {
        // Given
        testTransaction.setStatus(TransactionStatus.ANULLED);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        // When & Then
        assertThrows(TransactionAlreadyAnnulledException.class, () -> transactionService.annulTransaction(validAnulationRequest));
        verify(cardRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void annulTransaction_Expired_ThrowsTransactionExpiredException() {
        // Given
        testTransaction.setTransactionDate(LocalDateTime.now().minusHours(25));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        // When & Then
        assertThrows(TransactionExpiredException.class, () -> transactionService.annulTransaction(validAnulationRequest));
        verify(cardRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getAllTransactions_Successful() {
        // Given
        Transaction transaction2 = Transaction.builder()
                .transactionId(2L)
                .card(testCard)
                .price(new BigDecimal("200.00"))
                .status(TransactionStatus.ACTIVE)
                .transactionDate(LocalDateTime.now())
                .build();

        List<Transaction> transactions = Arrays.asList(testTransaction, transaction2);
        when(transactionRepository.findAll()).thenReturn(transactions);

        // When
        List<TransactionResponse> result = transactionService.getAllTransactions();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getTransactionId());
        assertEquals(2L, result.get(1).getTransactionId());
        verify(transactionRepository).findAll();
    }

    @Test
    void getAllTransactions_EmptyList() {
        // Given
        when(transactionRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<TransactionResponse> result = transactionService.getAllTransactions();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transactionRepository).findAll();
    }
}
