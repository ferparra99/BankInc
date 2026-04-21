package com.nexos.bankinc.util;

import com.nexos.bankinc.repository.CardRepository;
import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class CardNumberGenerator {

    private final CardRepository cardRepository;
    private final Random random = new Random();

    public CardNumberGenerator(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public String generate(String productId) {
        String cardNumber;
        do {
            cardNumber = productId + generateRandomDigits(10);
        } while (cardRepository.existsById(cardNumber));
        return cardNumber;
    }

    private String generateRandomDigits(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}