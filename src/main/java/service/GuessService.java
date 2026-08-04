package service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GuessService {
    private final Map<Long, Integer> secretNumbers = new HashMap<>();
    private final Random rand = new Random();

    public void startGame(long chatId){
        secretNumbers.put(chatId, rand.nextInt(1,10+1));

    }
    public Integer getSecretNumber(long chatId){
        if (secretNumbers.get(chatId) == null){
            return 0;
        }else{
            return secretNumbers.get(chatId);
        }
    }

    public void endGame(long chatId){
        secretNumbers.remove(chatId);
    }
}
