package service;

import model.HumanGuessResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GuessServiceTest {
    GuessService guessService = new GuessService();

    @Test
    void guessBelowSecretIsTooLow(){
        HumanGuessResult humanGuessResult = guessService.compare(2,1);
        assertEquals(HumanGuessResult.TOO_LOW, humanGuessResult);
    }

    @Test
    void guessAboveSecretIsTooHigh(){
        HumanGuessResult humanGuessResult = guessService.compare(2,3);
        assertEquals(HumanGuessResult.TOO_HIGH, humanGuessResult);
    }

    @Test
    void matchingGuessIsEqual(){
        HumanGuessResult humanGuessResult = guessService.compare(2,2);
        assertEquals(HumanGuessResult.EQUAL, humanGuessResult);
    }

    @Test
    void notStartedGameThrowsException(){
        assertThrows(IllegalStateException.class, () -> guessService.getGame(0));
    }
}
