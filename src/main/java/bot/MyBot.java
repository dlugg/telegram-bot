package bot;

import commands.*;
import model.State;
import okhttp3.OkHttpClient;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import service.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyBot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient; // инструмент для отправки
    private final StateService stateService = new StateService();
    private final GuessService guessService = new GuessService();
    private final RpsService rpsService = new RpsService();
    private final NameService nameService = new NameService();
    private final TaskService taskService = new TaskService();
    private final String weatherApiKey;
    private final Random rand = new Random();
    private final Map<String, Command> commands = new HashMap<>();
    private final OkHttpClient client = new OkHttpClient();
    private final Map<State, Command> stateCommands = new HashMap<>();

    // Конструктор: при создании бота мы передаем ему токен
    public MyBot(String botToken, String weatherApiKey) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.weatherApiKey = weatherApiKey;
        commands.put("/start", new StartCommand());
        commands.put("/help", new HelpCommand());
        commands.put("/todo", new ToDoCommand());
        commands.put("/list", new ListCommand(taskService));
        commands.put("/add", new AddCommand(taskService));
        commands.put("/done", new DoneCommand(taskService));
        commands.put("/remind", new RemindCommand(telegramClient));
        commands.put("/btc", new BtcCommand(client));
        commands.put("/quote", new QuoteCommand(client));
        ReverseCommand reverseCommand = new ReverseCommand(stateService);
        BallCommand ballCommand = new BallCommand(stateService);
        WeatherCommand weatherCommand = new WeatherCommand(stateService, client, weatherApiKey);
        GuessCommand guessCommand = new GuessCommand(stateService, guessService);
        RpsCommand rpsCommand = new RpsCommand(rpsService, stateService);
        WhoAreYouCommand whoAreYouCommand = new WhoAreYouCommand(nameService, stateService);
        commands.put("/weather", weatherCommand);
        stateCommands.put(State.WAITING_FOR_WEATHER_CITY, weatherCommand);
        commands.put("/ball", ballCommand);
        stateCommands.put(State.WAITING_FOR_QUESTION, ballCommand);
        commands.put("/reverse", reverseCommand);
        stateCommands.put(State.WAITING_FOR_REVERSE, reverseCommand);
        commands.put("/guess", guessCommand);
        stateCommands.put(State.WAITING_FOR_GUESS, guessCommand);
        commands.put("/rps", rpsCommand);
        stateCommands.put(State.WAITING_FOR_HUMAN_CHOICE, rpsCommand);
        commands.put("/stats", new StatsCommand(rpsService));
        commands.put("/who", whoAreYouCommand);
        stateCommands.put(State.WAITING_FOR_NAME, whoAreYouCommand);
        stateCommands.put(State.WAITING_FOR_CONFIRM, whoAreYouCommand);
    }


    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // 1. Вытаскиваем текст, который написал юзер
            String text = update.getMessage().getText();

            // 2. Вытаскиваем ID чата (чтобы бот знал, куда отвечать)
            long chatId = update.getMessage().getChatId();
            State currentState = stateService.getState(chatId);
            SendMessage message = new SendMessage(String.valueOf(chatId), "");

            if (currentState == State.IDLE) {
                String[] parts = text.split(" ", 2);
                Command cmd = commands.get(parts[0]);
                String args = "";
                if (cmd != null) {
                    if (parts.length > 1) {
                        args = parts[1];
                    }
                    message.setText(cmd.execute(chatId, args));
                } else {
                    message.setText("Я не понимаю. Напиши /help");
                }
            } else {
                Command stateCmd = stateCommands.get(currentState);
                if (stateCmd != null) {
                    message.setText(stateCmd.execute(chatId, text));
                }
            }
            // 4. Оборачиваем отправку в защиту от ошибок сети
            try {
                telegramClient.execute(message);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }
}

