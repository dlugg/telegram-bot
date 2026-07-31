import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.util.*;

public class MyBot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient; // инструмент для отправки
    private final StateService stateService = new StateService();
    private final Map<Long, String> usersNames = new HashMap<>();
    private final Map<Long, Integer> numberToGuess = new HashMap<>();
    private final Map<Long, Integer> rpsHumanGameStats = new HashMap<>();
    private final Map<Long, Integer> rpsComputerGameStats = new HashMap<>();
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

        BallCommand ballCommand = new BallCommand(stateService);
        WeatherCommand weatherCommand = new WeatherCommand(stateService, client, weatherApiKey);
        commands.put("/weather", weatherCommand);
        stateCommands.put(State.WAITING_FOR_WEATHER_CITY, weatherCommand);
        commands.put("/ball", ballCommand);
        stateCommands.put(State.WAITING_FOR_QUESTION, ballCommand);
    }


    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // 1. Вытаскиваем текст, который написал юзер
            String text = update.getMessage().getText();

            // 2. Вытаскиваем ID чата (чтобы бот знал, куда отвечать)
            long chatId = update.getMessage().getChatId();
            State currentState = stateService.getState(chatId);
            String currentUserName = usersNames.getOrDefault(chatId, "");
            int currentUserWins = rpsHumanGameStats.getOrDefault(chatId, 0);
            int currentUserloses = rpsComputerGameStats.getOrDefault(chatId, 0);
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
                    switch (text) {
                        case "/guess" -> {
                            numberToGuess.put(chatId, rand.nextInt(1, 10 + 1));
                            stateService.setState(chatId, State.WAITING_FOR_GUESS);
                            message.setText("Я загадал число от 1 до 10. Отгадывай!");
                        }

                        case "Кто ты?" -> {
                            message.setText("Я просто глупый робот. А как тебя зовут, человек?");
                            stateService.setState(chatId, State.WAITING_FOR_NAME);

                        }
                        case "/reverse" -> {
                            stateService.setState(chatId, State.WAITING_FOR_REVERSE);
                            message.setText("Напиши мне любое слово или фразу, и я разверну ее задом наперед!");
                        }
                        case "/rps" -> {
                            stateService.setState(chatId, State.WAITING_FOR_HUMAN_CHOICE);
                            message.setText("Давай сыграем в Камень, Ножницы, Бумага. Выбери свой ход (1-3): \n" +
                                    "1) Камень\n" +
                                    "2) Ножницы\n" +
                                    "3) Бумага\n\n" +
                                    "Проверить статистику можно командой /stats");
                        }
                        case "/stats" -> {
                            message.setText("Твои победы: " + currentUserWins + "| Мои победы: " + currentUserloses);
                        }
                        default -> message.setText("Я не понимаю. Напиши /help");
                    }
                }
            } else {
                Command stateCmd = stateCommands.get(currentState);
                if (stateCmd != null) {
                    message.setText(stateCmd.execute(chatId, text));


                } else if (currentState == State.WAITING_FOR_NAME) {
                    usersNames.put(chatId, text);
                    message.setText("Тебя действительно зовут " + text + "? Напиши Да или Нет.");
                    stateService.setState(chatId, State.WAITING_FOR_CONFIRM);
                } else if (currentState == State.WAITING_FOR_CONFIRM) {
                    if (text.equalsIgnoreCase("Да")) {
                        message.setText("Приятно познакомиться, " + currentUserName + "!");
                    } else {
                        message.setText("Извини, я перегрелся. Давай заново.");
                    }
                    stateService.setState(chatId, State.IDLE);
                } else if (currentState == State.WAITING_FOR_GUESS) {
                    try {
                        if (numberToGuess.get(chatId) > Integer.parseInt(text)) {
                            message.setText("Мое число больше! ");
                        } else if (numberToGuess.get(chatId) < Integer.parseInt(text)) {
                            message.setText("Мое число меньше! ");
                        } else {
                            message.setText("Угадал!");

                            stateService.setState(chatId, State.IDLE);
                        }
                    } catch (NumberFormatException e) {
                        message.setText("Пожалуйста, введи число цифрами!");
                    }
                } else if (currentState == State.WAITING_FOR_REVERSE) {
                    char[] letters = text.toCharArray();
                    for (int i = 0; i < letters.length / 2; i++) {
                        char temp = letters[i];
                        letters[i] = letters[letters.length - 1 - i];
                        letters[letters.length - 1 - i] = temp;
                    }
                    String reversedText = new String(letters);
                    message.setText(reversedText);
                    stateService.setState(chatId, State.IDLE);
                } else if (currentState == State.WAITING_FOR_HUMAN_CHOICE) {
                    try {
                        int humanChoice = Integer.parseInt(text);
                        int computerChoice = rand.nextInt(1, 3 + 1);
                        switch (humanChoice) {
                            case (1) -> {
                                if (computerChoice == 2) {
                                    message.setText("Я выбрал ножницы, Ты победил!");
                                    rpsHumanGameStats.put(chatId, ++currentUserWins);
                                    stateService.setState(chatId, State.IDLE);
                                } else if (computerChoice == 3) {
                                    message.setText("Я выбрал бумагу, Ты проиграл!");
                                    rpsComputerGameStats.put(chatId, ++currentUserloses);
                                    stateService.setState(chatId, State.IDLE);
                                } else {
                                    message.setText("Ничья!");
                                    stateService.setState(chatId, State.IDLE);
                                }
                            }
                            case (2) -> {
                                if (computerChoice == 1) {
                                    message.setText("Я выбрал камень, Ты проиграл!");
                                    rpsComputerGameStats.put(chatId, ++currentUserloses);
                                    stateService.setState(chatId, State.IDLE);
                                } else if (computerChoice == 3) {
                                    message.setText("Я выбрал бумагу, Ты победил!");
                                    rpsHumanGameStats.put(chatId, ++currentUserWins);
                                    stateService.setState(chatId, State.IDLE);
                                } else {
                                    message.setText("Ничья!");
                                    stateService.setState(chatId, State.IDLE);
                                }
                            }
                            case (3) -> {
                                if (computerChoice == 1) {
                                    message.setText("Я выбрал камень, Ты победил!");
                                    rpsHumanGameStats.put(chatId, ++currentUserWins);
                                    stateService.setState(chatId, State.IDLE);
                                } else if (computerChoice == 2) {
                                    message.setText("Я выбрал ножницы, Ты проиграл!");
                                    rpsComputerGameStats.put(chatId, ++currentUserloses);
                                    stateService.setState(chatId, State.IDLE);
                                } else {
                                    message.setText("Ничья!");
                                    stateService.setState(chatId, State.IDLE);
                                }
                            }
                            default -> {
                                message.setText("Я не понимаю введи /help");
                                stateService.setState(chatId, State.IDLE);
                            }
                        }
                    } catch (NumberFormatException e) {
                        message.setText("Пожалуйста, введи только цифру хода (1, 2 или 3)!");
                    }
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

