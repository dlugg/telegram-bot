import java.util.Random;

public class BallCommand implements Command {
    private final Random rand = new Random();
    private final StateService stateService;

    public BallCommand(StateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public String execute(long chatId, String args) {
        if (args.isBlank()) {
            stateService.setState(chatId, State.WAITING_FOR_QUESTION);
            return "Напиши свой вопрос (или сразу: /ball <Вопрос>)";
        } else {
            String[] answers = {"Бесспорно", "Даже не думай", "Мне кажется — да", "Пока не яснo", "Мой ответ — нет"};
            stateService.setState(chatId, State.IDLE);
            return answers[rand.nextInt(0, answers.length)];
        }
    }
}
