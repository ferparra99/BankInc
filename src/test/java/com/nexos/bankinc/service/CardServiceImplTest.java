package com.nexos.bankinc.service;

import com.nexos.bankinc.dto.request.BalanceRequest;
import com.nexos.bankinc.dto.request.EnrollCardRequest;
import com.nexos.bankinc.dto.response.BalanceResponse;
import com.nexos.bankinc.dto.response.CardResponse;
import com.nexos.bankinc.dto.response.RechargeResponse;
import com.nexos.bankinc.entity.Card;
import com.nexos.bankinc.exception.*;
import com.nexos.bankinc.repository.CardRepository;
import com.nexos.bankinc.util.CardNumberGenerator;
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
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @InjectMocks
    private CardServiceImpl cardService;

    private Card testCard;
    private EnrollCardRequest validEnrollRequest;
    private BalanceRequest validBalanceRequest;

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
                .createdAt(LocalDateTime.now())
                .build();

        validEnrollRequest = new EnrollCardRequest();
        validEnrollRequest.setCardId("1234567890123456");

        validBalanceRequest = new BalanceRequest();
        validBalanceRequest.setCardId("1234567890123456");
        validBalanceRequest.setBalance(new BigDecimal("500.00"));
    }

    @Test
    void generateCardNumber_Successful() {
        // Given
        String generatedCardNumber = "1234567890123456";
        when(cardNumberGenerator.generate(anyString())).thenReturn(generatedCardNumber);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardRepository.findById(generatedCardNumber)).thenReturn(Optional.of(testCard));

        // When
        CardResponse result = cardService.generateCardNumber("123456");

        // Then
        assertNotNull(result);
        assertEquals("1234567890123456", result.getCardId());
        assertEquals("123456", result.getProductId());
        assertEquals("Test User", result.getClientName());
        assertEquals(LocalDate.now().plusMonths(6), result.getExpirationDate());
        assertEquals(new BigDecimal("1000.00"), result.getBalance());
        assertTrue(result.isActive());
        assertFalse(result.isBlocked());

        verify(cardNumberGenerator).generate("123456");
        verify(cardRepository).save(any(Card.class));
        verify(cardRepository).findById(generatedCardNumber);
    }

    @Test
    void generateCardNumber_CardNotFoundAfterSave_ThrowsCardNotFoundException() {
        // Given
        String generatedCardNumber = "1234567890123456";
        when(cardNumberGenerator.generate(anyString())).thenReturn(generatedCardNumber);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardRepository.findById(generatedCardNumber)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CardNotFoundException.class, () -> cardService.generateCardNumber("123456"));
        verify(cardNumberGenerator).generate("123456");
        verify(cardRepository).save(any(Card.class));
        verify(cardRepository).findById(generatedCardNumber);
    }

    @Test
    void enrollCard_Successful() {
        // Given
        testCard.setActive(false);
        when(cardRepository.findById(anyString())).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // When
        cardService.enrollCard(validEnrollRequest);

        // Then
        verify(cardRepository).save(testCard);
        assertTrue(testCard.isActive());
    }

    @Test
    void enrollCard_CardNotFound_ThrowsCardNotFoundException() {
        // Given
        when(cardRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CardNotFoundException.class, () -> cardService.enrollCard(validEnrollRequest));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void blockCard_Successful() {
        // Given
        when(cardRepository.findById(anyString())).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // When
        cardService.blockCard("1234567890123456");

        // Then
        verify(cardRepository).save(testCard);
        assertTrue(testCard.isBlocked());
    }

    @Test
    void blockCard_CardNotFound_ThrowsCardNotFoundException() {
        // Given
        when(cardRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CardNotFoundException.class, () -> cardService.blockCard("1234567890123456"));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void rechargeBalance_Successful() {
        // Given
        when(cardRepository.findById(anyString())).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // When
        RechargeResponse result = cardService.rechargeBalance(validBalanceRequest);

        // Then
        assertNotNull(result);
        assertEquals("Recarga exitosa", result.getMessage());
        assertEquals("1234567890123456", result.getCardId());
        assertEquals(new BigDecimal("1500.00"), result.getBalance());

        verify(cardRepository).save(testCard);
        assertEquals(new BigDecimal("1500.00"), testCard.getBalance());
    }

    @Test
    void rechargeBalance_CardNotFound_ThrowsCardNotFoundException() {
        // Given
        when(cardRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CardNotFoundException.class, () -> cardService.rechargeBalance(validBalanceRequest));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void rechargeBalance_CardNotActive_ThrowsCardNotActiveException() {
        // Given
        testCard.setActive(false);
        when(cardRepository.findById(anyString())).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(CardNotActiveException.class, () -> cardService.rechargeBalance(validBalanceRequest));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void rechargeBalance_CardBlocked_ThrowsCardBlockedException() {
        // Given
        testCard.setBlocked(true);
        when(cardRepository.findById(anyString())).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(CardBlockedException.class, () -> cardService.rechargeBalance(validBalanceRequest));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void rechargeBalance_CardExpired_ThrowsCardExpiredException() {
        // Given
        testCard.setExpirationDate(LocalDate.now().minusDays(1));
        when(cardRepository.findById(anyString())).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(CardExpiredException.class, () -> cardService.rechargeBalance(validBalanceRequest));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void rechargeBalance_InvalidAmount_ThrowsIllegalArgumentException() {
        // Given
        validBalanceRequest.setBalance(BigDecimal.ZERO);
        when(cardRepository.findById(anyString())).thenReturn(Optional.of(testCard));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> cardService.rechargeBalance(validBalanceRequest));
        assertEquals("El monto de recarga mayor a 0", exception.getMessage());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void rechargeBalance_NegativeAmount_ThrowsIllegalArgumentException() {
        // Given
        validBalanceRequest.setBalance(new BigDecimal("-100.00"));
        when(cardRepository.findById(anyString())).thenReturn(Optional.of(testCard));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> cardService.rechargeBalance(validBalanceRequest));
        assertEquals("El monto de recarga mayor a 0", exception.getMessage());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void getBalance_Successful() {
        // Given
        when(cardRepository.findById(anyString())).thenReturn(Optional.of(testCard));

        // When
        BalanceResponse result = cardService.getBalance("1234567890123456");

        // Then
        assertNotNull(result);
        assertEquals("1234567890123456", result.getCardId());
        assertEquals(new BigDecimal("1000.00"), result.getBalance());
        verify(cardRepository).findById("1234567890123456");
    }

    @Test
    void getBalance_CardNotFound_ThrowsCardNotFoundException() {
        // Given
        when(cardRepository.findById(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CardNotFoundException.class, () -> cardService.getBalance("1234567890123456"));
        verify(cardRepository).findById("1234567890123456");
    }

    @Test
    void findCards_Successful() {
        // Given
        Card card2 = Card.builder()
                .cardId("9876543210987654")
                .productId("654321")
                .clientName("Another User")
                .expirationDate(LocalDate.now().plusMonths(3))
                .balance(new BigDecimal("500.00"))
                .active(true)
                .blocked(false)
                .createdAt(LocalDateTime.now().minusDays(10))
                .build();

        List<Card> cards = Arrays.asList(testCard, card2);
        when(cardRepository.findAll()).thenReturn(cards);

        // When
        List<CardResponse> result = cardService.findCards();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        CardResponse firstCard = result.get(0);
        assertEquals("1234567890123456", firstCard.getCardId());
        assertEquals("123456", firstCard.getProductId());
        assertEquals("Test User", firstCard.getClientName());

        CardResponse secondCard = result.get(1);
        assertEquals("9876543210987654", secondCard.getCardId());
        assertEquals("654321", secondCard.getProductId());
        assertEquals("Another User", secondCard.getClientName());

        verify(cardRepository).findAll();
    }

    @Test
    void findCards_EmptyList() {
        // Given
        when(cardRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<CardResponse> result = cardService.findCards();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cardRepository).findAll();
    }
}
