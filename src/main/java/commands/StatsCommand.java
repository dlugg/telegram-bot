package commands;

import service.RpsService;

public class StatsCommand implements Command {
    private final RpsService rpsService;
    public StatsCommand(RpsService rpsService){
        this.rpsService = rpsService;
    }
    @Override
    public String execute(long chatId, String args) {
        return rpsService.getStats(chatId);
    }
    @Override
    public String description(){
        return "твои победы/поражения в камень, ножницы, бумага";
    }
}
