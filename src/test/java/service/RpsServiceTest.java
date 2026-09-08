package service;

import model.RpsGameResult;
import model.RpsMove;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RpsServiceTest {
    RpsService rpsService = new RpsService();

    @Test
    void sameMovesResultInDraw() {
        assertEquals(RpsGameResult.DRAW, rpsService.determineResult(RpsMove.ROCK, RpsMove.ROCK));
        assertEquals(RpsGameResult.DRAW, rpsService.determineResult(RpsMove.PAPER, RpsMove.PAPER));
        assertEquals(RpsGameResult.DRAW, rpsService.determineResult(RpsMove.SCISSORS, RpsMove.SCISSORS));
    }

    @Test
    void strongerMoveWins() {
        assertEquals(RpsGameResult.WIN, rpsService.determineResult(RpsMove.PAPER, RpsMove.ROCK));
        assertEquals(RpsGameResult.WIN, rpsService.determineResult(RpsMove.ROCK, RpsMove.SCISSORS));
        assertEquals(RpsGameResult.WIN, rpsService.determineResult(RpsMove.SCISSORS, RpsMove.PAPER));
    }

    @Test
    void weakerMoveLoses() {
        assertEquals(RpsGameResult.LOSE, rpsService.determineResult(RpsMove.PAPER, RpsMove.SCISSORS));
        assertEquals(RpsGameResult.LOSE, rpsService.determineResult(RpsMove.SCISSORS, RpsMove.ROCK));
        assertEquals(RpsGameResult.LOSE, rpsService.determineResult(RpsMove.ROCK, RpsMove.PAPER));
    }

    @Test
    void humanMove() {
        assertThrows(IllegalArgumentException.class, () -> rpsService.humanMove(0));
        assertThrows(IllegalArgumentException.class, () -> rpsService.humanMove(4));
        assertThrows(IllegalArgumentException.class, () -> rpsService.humanMove(-2));
    }

}
