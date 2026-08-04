public class StatsCommand implements Command{
    private final RpsService rpsService;
    StatsCommand(RpsService rpsService){
        this.rpsService = rpsService;
    }
    @Override
    public String execute(long chatId, String args) {
        return rpsService.getStats(chatId);
    }
}
