package commands;

import model.State;
import service.NameService;
import service.StateService;


public class WhoAreYouCommand implements Command {
    private final NameService nameService;
    private final StateService stateService;

    public WhoAreYouCommand(NameService nameService, StateService stateService) {
        this.nameService = nameService;
        this.stateService = stateService;
    }

    @Override
    public String execute(long chatId, String args) {

        if (args.isBlank()) {
            stateService.setState(chatId, State.WAITING_FOR_NAME);
            return "Я просто глупый робот. А как тебя зовут, человек?";
        } else if (stateService.getState(chatId) == State.WAITING_FOR_NAME) {
            stateService.setState(chatId, State.WAITING_FOR_CONFIRM);
            if (nameService.saveUserName(chatId, args)) {
                return "Тебя действительно зовут " + args + "? Напиши Да или Нет.";
            } else {
                return "Что-то пошло не так..";
            }


        } else {
            if (args.equalsIgnoreCase("Да")) {
                stateService.setState(chatId, State.IDLE);
                return ("Приятно познакомиться, " + nameService.getUserName(chatId) + "!");
            } else {
                nameService.deleteUserName(chatId);
                stateService.setState(chatId, State.IDLE);
                return "Извини, я перегрелся. Давай заново. ";
            }
        }
    }


    @Override
    public String description() {
        return "познакомится";
    }
}

