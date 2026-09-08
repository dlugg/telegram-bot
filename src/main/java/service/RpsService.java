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
        if (humanChoice<1 || humanChoice >3){
            throw new IllegalArgumentException("ход должен быть от 1 до 3, получено: " + humanChoice);
        }else{
            return moves[humanChoice - 1];
        }
    }

    public RpsGameResult determineResult(RpsMove human, RpsMove computer) {
        if (human == computer) {
            return RpsGameResult.DRAW;
        } else if (human == RpsMove.ROCK && computer == RpsMove.SCISSORS ||
                human == RpsMove.SCISSORS && computer == RpsMove.PAPER ||
                human == RpsMove.PAPER && computer == RpsMove.ROCK) {
            return RpsGameResult.WIN;
        } else {
            return RpsGameResult.LOSE;
        }
    }

    public RpsRoundResult rpsRoundResult(long chatId, int humanChoice) {
        RpsMove computerMove = computerMove();
        RpsMove humanMove = humanMove(humanChoice);
        RpsGameResult gameResult = determineResult(humanMove, computerMove);
        if (gameResult == RpsGameResult.WIN) {
            addWin(chatId);
        } else if (gameResult == RpsGameResult.LOSE) {
            addLoss(chatId);
        }
        return new RpsRoundResult(computerMove, gameResult);
    }
}

