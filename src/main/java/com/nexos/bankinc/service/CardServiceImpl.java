package com.nexos.bankinc.service;

import com.nexos.bankinc.dto.request.BalanceRequest;
import com.nexos.bankinc.dto.request.EnrollCardRequest;
import com.nexos.bankinc.dto.response.BalanceResponse;
import com.nexos.bankinc.dto.response.CardResponse;
import com.nexos.bankinc.dto.response.RechargeResponse;
import com.nexos.bankinc.entity.Card;
import com.nexos.bankinc.exception.CardBlockedException;
import com.nexos.bankinc.exception.CardExpiredException;
import com.nexos.bankinc.exception.CardNotActiveException;
import com.nexos.bankinc.exception.CardNotFoundException;
import com.nexos.bankinc.repository.CardRepository;
import com.nexos.bankinc.util.CardNumberGenerator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardNumberGenerator cardNumberGenerator;

    public CardServiceImpl(CardRepository cardRepository,
                           CardNumberGenerator cardNumberGenerator) {
        this.cardRepository = cardRepository;
        this.cardNumberGenerator = cardNumberGenerator;
    }

    //llamado a la generacion de #tarjeta y guardado en la base de datos
    //Deberia guardar el nombre del cliente
    @Override
    public CardResponse generateCardNumber(String productId) {
        String cardNumber = cardNumberGenerator.generate(productId);
        Card card = Card.builder()
                .cardId(cardNumber)
                .productId(productId)
                .clientName("Nombre Cliente")
                .build();
        cardRepository.save(card);

        Card findCard = cardRepository.findById(cardNumber)
                .orElseThrow(() -> new CardNotFoundException(cardNumber));

        return new CardResponse(
                findCard.getCardId(),
                findCard.getProductId(),
                findCard.getClientName(),
                findCard.getExpirationDate(),
                findCard.getBalance(),
                findCard.isActive(),
                findCard.isBlocked(),
                findCard.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public void enrollCard(EnrollCardRequest request) {
        Card card = findCardOrThrow(request.getCardId());
        card.setActive(true);
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public String blockCard(String cardId) {
        String statusCard;
        Card card = findCardOrThrow(cardId);
        if (!card.isBlocked()){
            card.setBlocked(true) ;
            cardRepository.save(card);
            statusCard = "Tarjeta bloqueada con éxito";
        }else {
            statusCard = "Tarjeta ya se encuentra bloqueada";
        }
        return statusCard;
    }

    @Override
    @Transactional
    public RechargeResponse rechargeBalance(BalanceRequest request) {
        Card card = findCardOrThrow(request.getCardId());

        if (!card.isActive()) throw new CardNotActiveException();
        if (card.isBlocked()) throw new CardBlockedException();
        if (card.getExpirationDate().isBefore(LocalDate.now())) throw new CardExpiredException();
        if (request.getBalance().signum() <= 0) {
            throw new IllegalArgumentException("El monto de recarga mayor a 0");
        }

        card.setBalance(card.getBalance().add(request.getBalance()));
        cardRepository.save(card);

        return new RechargeResponse(
                "Recarga exitosa",
                request.getCardId(),
                card.getBalance()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(String cardId) {
        Card card = findCardOrThrow(cardId);
        return new BalanceResponse(card.getCardId(), card.getBalance());
    }

    @Override
    @Transactional
    public String activeCard(String cardId) {
        String statusCard;
        Card card = findCardOrThrow(cardId);
        if (card.isBlocked()){
            card.setBlocked(false) ;
            cardRepository.save(card);
            statusCard = "Tarjeta desbloqueada con éxito";
        }else {
            statusCard = "Tarjeta ya se encuentra desbloqueada";
        }
        return statusCard;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardResponse> findCards() {
        List<Card> cards = cardRepository.findAll();

        return cards.stream()
                .map(card -> new CardResponse(
                        card.getCardId(),
                        card.getProductId(),
                        card.getClientName(),
                        card.getExpirationDate(),
                        card.getBalance(),
                        card.isActive(),
                        card.isBlocked(),
                        card.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    private Card findCardOrThrow(String cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }


}