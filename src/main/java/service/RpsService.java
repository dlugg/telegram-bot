package service;

import model.RpsGameResult;
import model.RpsMove;
import model.RpsRoundResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RpsService {
    private final Map<Long, Score> userRpsStats = new HashMap<>();
    private final Random rand = new Random();

    public String getStats(long chatId) {
        if (userRpsStats.containsKey(chatId)) {
            return "Победы : " + userRpsStats.get(chatId).getWins() + " | Поражения: " + userRpsStats.get(chatId).getLosses();
        } else {
            return "Ты еще не играл.";
        }
    }

    public void addWin(long chatId) {
        userRpsStats.computeIfAbsent(chatId, k -> new Score()).addWin();
    }

    public void addLoss(long chatId) {
        if (!userRpsStats.containsKey(chatId)) {
            userRpsStats.put(chatId, new Score());
        }
        userRpsStats.get(chatId).addLoss();
    }


    public RpsMove computerMove() {
        RpsMove[] moves = RpsMove.values();
        int computerChoice = rand.nextInt(0, moves.length);
        return moves[computerChoice];
    }

    public RpsMove humanMove(int humanChoice) {
        RpsMove[] moves = RpsMove.values();
        return moves[humanChoice - 1];
    }

    public RpsRoundResult rpsRoundResult(long chatId, int humanChoice) {
        RpsMove computerMove = computerMove();
        RpsMove humanMove = humanMove(humanChoice);
        if (humanMove == computerMove) {
            return new RpsRoundResult(computerMove, RpsGameResult.DRAW);
        } else if (humanMove == RpsMove.ROCK && computerMove == RpsMove.SCISSORS ||
                humanMove == RpsMove.SCISSORS && computerMove == RpsMove.PAPER ||
                humanMove == RpsMove.PAPER && computerMove == RpsMove.ROCK) {
            addWin(chatId);
            return new RpsRoundResult(computerMove, RpsGameResult.WIN);
        } else {
            addLoss(chatId);
            return new RpsRoundResult(computerMove, RpsGameResult.LOSE);
        }
    }
}
