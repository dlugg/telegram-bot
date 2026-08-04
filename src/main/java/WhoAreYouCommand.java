public class WhoAreYouCommand implements Command {
    private final NameService nameService;
    private final StateService stateService;

    WhoAreYouCommand(NameService nameService, StateService stateService) {
        this.nameService = nameService;
        this.stateService = stateService;
    }

    @Override
    public String execute(long chatId, String args) {

        if (args.isBlank()) {
            stateService.setState(chatId, State.WAITING_FOR_NAME);
            return "Я просто глупый робот. А как тебя зовут, человек?";
        } else if (stateService.getState(chatId) == State.WAITING_FOR_NAME) {
            nameService.setUserName(chatId, args);
            stateService.setState(chatId, State.WAITING_FOR_CONFIRM);
            return "Тебя действительно зовут " + args + "? Напиши Да или Нет.";


        } else {
            if (args.equalsIgnoreCase("Да")) {
                stateService.setState(chatId, State.IDLE);
                return ("Приятно познакомиться, " + nameService.getName(chatId) + "!");
            } else {
                nameService.removeUserName(chatId);
                stateService.setState(chatId, State.IDLE);
                return "Извини, я перегрелся. Давай заново. ";
            }
        }

    }
}
