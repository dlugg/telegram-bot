package commands;

import model.RpsGameResult;
import service.RpsService;

import java.util.Map;

public class StatsCommand implements Command {
    private final RpsService rpsService;

    public StatsCommand(RpsService rpsService) {
        this.rpsService = rpsService;
    }

    @Override
    public String execute(long chatId, String args) {
        Map<RpsGameResult, Integer> userRpsStats = rpsService.getStats(chatId);
        StringBuilder result = new StringBuilder();
        for (RpsGameResult rpsGameResult : RpsGameResult.values()) {
            result.append(rpsGameResult.getTitle()).append(": ").append(userRpsStats.getOrDefault(rpsGameResult,0)).append("\n");
        }
        return result.toString();
    }

    @Override
    public String description() {
        return "твои победы/поражения в камень, ножницы, бумага";
    }
}
