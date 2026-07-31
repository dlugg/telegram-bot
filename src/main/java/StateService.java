import java.util.HashMap;
import java.util.Map;

public class StateService {
    private final Map<Long, State> states = new HashMap<>();

    public State getState(Long userId) {
        return states.getOrDefault(userId, State.IDLE);
    }

    public void setState(Long userId, State state){
        states.put(userId, state);
    }
}
