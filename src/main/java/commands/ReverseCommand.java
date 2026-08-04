package commands;

import model.State;
import service.StateService;

public class ReverseCommand implements Command {
    private final StateService stateService;

    public ReverseCommand(StateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public String execute(long chatId, String args) {
        if (args.isBlank()) {
            stateService.setState(chatId, State.WAITING_FOR_REVERSE);
            return "Напиши свою фразу (или сразу /reverse <фраза>)";
        } else {
            char[] letters = args.toCharArray();
            for (int i = 0; i < letters.length / 2; i++) {
                char temp = letters[i];
                letters[i] = letters[letters.length - 1 - i];
                letters[letters.length - 1 - i] = temp;
            }
            String reversedText = new String(letters);
            stateService.setState(chatId, State.IDLE);
            return reversedText;
        }

    }
}