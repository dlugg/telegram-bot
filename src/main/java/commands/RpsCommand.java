package commands;

import model.State;
import service.RpsService;
import service.StateService;

import java.util.Random;

public class RpsCommand implements Command {
    private final RpsService rpsService;
    private final StateService stateService;
    Random rand = new Random();
    private static final String[] MOVES = {"камень", "ножницы", "бумагу"};

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
                int computerChoice = rand.nextInt(1, 3 + 1);
                if (humanChoice < 4 && humanChoice > 0) {
                    if (humanChoice == computerChoice) {

                        stateService.setState(chatId, State.IDLE);
                        return "Компьютер выбрал : " + MOVES[computerChoice-1] + ". Ничья!";

                    } else if (computerChoice == humanChoice % 3 + 1) {
                        rpsService.addWin(chatId);
                        stateService.setState(chatId, State.IDLE);
                        return "Компьютер выбрал : " + MOVES[computerChoice-1] + ". Ты победил!";
                    } else {
                        rpsService.addLoss(chatId);
                        stateService.setState(chatId, State.IDLE);
                        return "Компьютер выбрал : " + MOVES[computerChoice-1] + ". Ты проиграл!";
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
    public String description(){
        return "сыграть в камень, ножницы, бумага";
    }
}

