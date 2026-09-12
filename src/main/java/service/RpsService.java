package service;

import exception.DataAccessException;
import model.RpsGameResult;
import model.RpsMove;
import model.RpsRoundResult;
import repository.RpsRoundsRepository;

import java.sql.SQLException;
import java.util.Map;
import java.util.Random;

public class RpsService {
    private final RpsRoundsRepository rpsRoundsRepository;
    private final Random rand = new Random();

    public RpsService(RpsRoundsRepository rpsRoundsRepository) {
        this.rpsRoundsRepository = rpsRoundsRepository;
    }


    public Map<RpsGameResult, Integer> getStats(long chatId) {
        try {
            return rpsRoundsRepository.getStats(chatId);
        } catch (SQLException e) {
            throw new DataAccessException("не удалось получить статистику игры пользователя " + chatId, e);
        }
    }


    public RpsMove computerMove() {
        RpsMove[] moves = RpsMove.values();
        int computerChoice = rand.nextInt(0, moves.length);
        return moves[computerChoice];
    }

    public RpsMove humanMove(int humanChoice) {
        RpsMove[] moves = RpsMove.values();
        if (humanChoice < 1 || humanChoice > 3) {
            throw new IllegalArgumentException("ход должен быть от 1 до 3, получено: " + humanChoice);
        } else {
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
        try {
            rpsRoundsRepository.addRound(chatId, gameResult);

        } catch (SQLException e) {
            throw new DataAccessException("не удалось добавить раунд в базу данных для пользователя " + chatId, e);
        }
        return new RpsRoundResult(computerMove, gameResult);

    }
}

