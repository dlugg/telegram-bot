import java.util.HashMap;
import java.util.Map;

public class RpsService {
    private final Map<Long, Score> userRpsStats = new HashMap<>();

    public String getStats(long chatId) {
        if (userRpsStats.containsKey(chatId)){
            return "Победы : " + userRpsStats.get(chatId).getWins() + " | Поражения: " + userRpsStats.get(chatId).getLosses();
        }else{
            return "Ты еще не играл.";
        }
    }

    public void addWin(long chatId) {
        if (!userRpsStats.containsKey(chatId)) {
            userRpsStats.put(chatId, new Score());
        }
        userRpsStats.get(chatId).addWin();
    }

    public void addLoss(long chatId){
        if(!userRpsStats.containsKey(chatId)){
            userRpsStats.put(chatId, new Score());
        }
        userRpsStats.get(chatId).addLoss();
    }
}
