package commands;

import model.RpsGameResult;
import model.RpsRoundResult;
import model.State;
import service.RpsService;
import service.StateService;


public class RpsCommand implements Command {
    private final RpsService rpsService;
    private final StateService stateService;

    public RpsCommand(RpsService rpsService, StateService stateService) {
        this.rpsService = rpsService;
        this.stateService = stateService;
    }


    @Override
    public String execute(long chatId, String args) {
        if (args.isBlank()) {
            stateService.setState(chatId, State.WAITING_FOR_HUMAN_CHOICE);
            return """
                    
                    Давай сыграем в Камень, Ножницы, Бумага. Выбери свой ход (1-3):
                    1) Камень
                    2) Ножницы
                    3) Бумага
                    Проверить статистику можно командой /stats""";

        } else {
            try {
                int humanChoice = Integer.parseInt(args);
                if (humanChoice < 4 && humanChoice > 0) {
                    RpsRoundResult rpsRoundResult = rpsService.rpsRoundResult(chatId, humanChoice);
                    stateService.setState(chatId, State.IDLE);
                    if (rpsRoundResult.getRpsGameResult() == RpsGameResult.DRAW) {

                        return "Компьютер выбрал : " + rpsRoundResult.getComputerMove().getTitle() + ". Ничья!";
                    } else if (rpsRoundResult.getRpsGameResult() == RpsGameResult.WIN) {
                        return "Компьютер выбрал : " + rpsRoundResult.getComputerMove().getTitle() + ". Ты победил!";
                    } else {
                        return "Компьютер выбрал : " + rpsRoundResult.getComputerMove().getTitle() + ". Ты проиграл!";
                    }
                } else {
                    return "Выбери между 1 и 3.";
                }
            } catch
            (NumberFormatException e) {
                return "Введи свой ход цифрой пожалуйста.";
            }
        }
    }

    @Override
    public String description() {
        return "сыграть в камень, ножницы, бумага";
    }
}

