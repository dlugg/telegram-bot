package commands;

import model.HumanGuessResult;
import model.State;
import service.GuessService;
import service.StateService;

public class GuessCommand implements Command {
    private final StateService stateService;
    private final GuessService guessService;

    public GuessCommand(StateService stateService, GuessService guessService) {
        this.stateService = stateService;
        this.guessService = guessService;
    }

    @Override
    public String execute(long chatId, String args) {
        if (args.isBlank()) {
            guessService.startGame(chatId);
            stateService.setState(chatId, State.WAITING_FOR_GUESS);
            return "Я загадал число от 1 до 10. Отгадывай!";
        } else {
            try {
                int humanGuess = Integer.parseInt(args);
                HumanGuessResult humanGuessResult = guessService.humanGuessResult(chatId,humanGuess);
                if (humanGuessResult == HumanGuessResult.TOO_LOW) {
                    return "Мое число больше! ";
                } else if (humanGuessResult == HumanGuessResult.TOO_HIGH) {
                    return "Мое число меньше! ";
                } else {
                    stateService.setState(chatId, State.IDLE);
                    guessService.endGame(chatId);
                    return "Угадал!";
                }
            } catch (NumberFormatException e) {
                return "Пожалуйста, введи число цифрами!";
            }
        }
    }
    @Override
    public String description(){
        return "отгадать число";
    }
}
