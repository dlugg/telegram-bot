package repository;

import model.RpsGameResult;
import model.RpsMove;
import model.RpsRoundResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RpsRoundsRepositoryTest {
    Database database = new Database("jdbc:postgresql://localhost:5432/javabot_test", "postgres", System.getenv("DATABASE_PASSWORD"));
    UserRepository userRepository = new UserRepository(database);
    RpsRoundsRepository rpsRoundsRepository = new RpsRoundsRepository(database, userRepository);

    @BeforeEach
    void clearRpsRoundsAndUsersTables() throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("TRUNCATE tasks, users, rps_rounds, guess_games RESTART IDENTITY")) {
            statement.execute();
        }
    }

    @Test
    void addedRoundAppearsAtStats() throws SQLException {
        long chatId = 123;
        RpsRoundResult rpsRoundResult = new RpsRoundResult(RpsMove.ROCK, RpsGameResult.WIN);
        rpsRoundsRepository.addRound(chatId,rpsRoundResult.getRpsGameResult());
        Map<RpsGameResult, Integer> rpsGameResultIntegerMap = rpsRoundsRepository.getStats(chatId);
        assertEquals(1, rpsGameResultIntegerMap.get(RpsGameResult.WIN));
        assertEquals(0, rpsGameResultIntegerMap.get(RpsGameResult.LOSE));
        assertEquals(0, rpsGameResultIntegerMap.get(RpsGameResult.DRAW));
    }

    @Test
    void manyAddedRoundAppearsSumsUpAtStats() throws SQLException {
        long chatId = 123;
        RpsRoundResult rpsRoundResult1 = new RpsRoundResult(RpsMove.ROCK, RpsGameResult.WIN);
        RpsRoundResult rpsRoundResult2 = new RpsRoundResult(RpsMove.ROCK, RpsGameResult.LOSE);
        rpsRoundsRepository.addRound(chatId, rpsRoundResult1.getRpsGameResult());
        rpsRoundsRepository.addRound(chatId, rpsRoundResult1.getRpsGameResult());
        rpsRoundsRepository.addRound(chatId, rpsRoundResult2.getRpsGameResult());
        Map<RpsGameResult, Integer> rpsGameResultIntegerMap = rpsRoundsRepository.getStats(chatId);
        assertEquals(2, rpsGameResultIntegerMap.get(RpsGameResult.WIN));
        assertEquals(1, rpsGameResultIntegerMap.get(RpsGameResult.LOSE));
        assertEquals(0, rpsGameResultIntegerMap.get(RpsGameResult.DRAW));
    }

    @Test
    void userSeeOnlyHesStats() throws SQLException {
        long chatIdFirstUser = 123;
        long chatIdSecondUser = 456;
        RpsRoundResult rpsRoundResult1 = new RpsRoundResult(RpsMove.ROCK, RpsGameResult.WIN);
        RpsRoundResult rpsRoundResult2 = new RpsRoundResult(RpsMove.ROCK, RpsGameResult.LOSE);

        rpsRoundsRepository.addRound(chatIdFirstUser, rpsRoundResult1.getRpsGameResult());
        rpsRoundsRepository.addRound(chatIdFirstUser, rpsRoundResult1.getRpsGameResult());
        rpsRoundsRepository.addRound(chatIdSecondUser, rpsRoundResult1.getRpsGameResult());
        rpsRoundsRepository.addRound(chatIdSecondUser, rpsRoundResult2.getRpsGameResult());

        Map<RpsGameResult, Integer> rpsGameResultIntegerMapFirstUser = rpsRoundsRepository.getStats(chatIdFirstUser);

        Map<RpsGameResult, Integer> rpsGameResultIntegerMapSecondUser = rpsRoundsRepository.getStats(chatIdSecondUser);

        assertEquals(2, rpsGameResultIntegerMapFirstUser.get(RpsGameResult.WIN));
        assertEquals(1, rpsGameResultIntegerMapSecondUser.get(RpsGameResult.WIN));

        assertEquals(0, rpsGameResultIntegerMapFirstUser.get(RpsGameResult.LOSE));
        assertEquals(1, rpsGameResultIntegerMapSecondUser.get(RpsGameResult.LOSE));
        assertEquals(0, rpsGameResultIntegerMapFirstUser.get(RpsGameResult.DRAW));
        assertEquals(0, rpsGameResultIntegerMapSecondUser.get(RpsGameResult.DRAW));
    }

    @Test
    void nonExistingChatIdReturnsZeroesAtStats() throws SQLException{
        Map<RpsGameResult, Integer> rpsGameResultIntegerMap = rpsRoundsRepository.getStats(123);
        assertEquals(0, rpsGameResultIntegerMap.get(RpsGameResult.WIN));
        assertEquals(0, rpsGameResultIntegerMap.get(RpsGameResult.LOSE));
        assertEquals(0, rpsGameResultIntegerMap.get(RpsGameResult.DRAW));
    }


}
