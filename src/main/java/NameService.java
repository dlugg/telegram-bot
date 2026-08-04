import java.util.HashMap;
import java.util.Map;

public class NameService {
    private final Map<Long, String> userNames = new HashMap<>();

    public String getName(long chatId) {
        return userNames.getOrDefault(chatId, "Я не знаю как тебя зовут :(");
    }

    public void setUserName(long chatId, String name) {
        userNames.put(chatId, name);
    }

    public void removeUserName(long chatId){
        userNames.remove(chatId);
    }
}
