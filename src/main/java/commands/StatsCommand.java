package commands;

import model.GuessStats;
import model.RpsGameResult;
import service.GuessService;
import service.RpsService;

import java.util.Map;

public class StatsCommand implements Command {
    private final RpsService rpsService;
    private final GuessService guessService;

    public StatsCommand(RpsService rpsService, GuessService guessService) {
        this.rpsService = rpsService;
        this.guessService = guessService;
    }

    @Override
    public String execute(long chatId, String args) {
        Map<RpsGameResult, Integer> userRpsStats = rpsService.getStats(chatId);
        GuessStats guessStats = guessService.getStats(chatId);
        StringBuilder result = new StringBuilder();
        result.append("Статистика игры в Камень-Ножницы-Бумага\n");
        for (RpsGameResult rpsGameResult : RpsGameResult.values()) {

                    result.append(rpsGameResult.getTitle())
                    .append(": ")
                    .append(userRpsStats.getOrDefault(rpsGameResult, 0))
                    .append("\n");
        }
        result.append("\nСтатистика игры в Угадайку\n");
        if (guessStats.getGamesPlayed() == 0) {
            result.append("Ты еще не играл");
        } else {
            result.append("Количество сыгранных игр: ")
                    .append(guessStats.getGamesPlayed())
                    .append("\n")
                    .append("Минимальное количество попыток: ").append(guessStats.getMinAttempts());
        }

        return result.toString();
    }

    @Override
    public String description() {
        return "статистика игр";
    }
}
