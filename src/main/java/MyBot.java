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
    private TelegramClient telegramClient; // инструмент для отправки
    private Map<Long, State> states = new HashMap<>();        // тут мы сохраним имя юзера
    private Map<Long, String> usersNames = new HashMap<>();
    private Map<Long, Integer> numberToGuess = new HashMap<>();
    private Map<Long, Integer> rpsHumanGameStats = new HashMap<>();
    private Map<Long, Integer> rpsComputerGameStats = new HashMap<>();
    private final TaskService taskService = new TaskService();
    private final String weatherApiKey;
    private final Random rand = new Random();
    private final Map<String, Command> commands = new HashMap<>();
    OkHttpClient client = new OkHttpClient();

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
    }


    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // 1. Вытаскиваем текст, который написал юзер
            String text = update.getMessage().getText();

            // 2. Вытаскиваем ID чата (чтобы бот знал, куда отвечать)
            long chatId = update.getMessage().getChatId();
            State currentState = states.getOrDefault(chatId, State.IDLE);
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
                            states.put(chatId, State.WAITING_FOR_GUESS);
                            message.setText("Я загадал число от 1 до 10. Отгадывай!");
                        }
                        case "/btc" -> {
                            String url = "https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT";
                            Request request = new Request.Builder().url(url).build();
                            try (Response response = client.newCall(request).execute()) {
                                String jsonResponse = response.body().string();
                                JSONObject obj = new JSONObject(jsonResponse);
                                double btcPrice = obj.getDouble("price");
                                message.setText("Текущая цена BTC/USD составляет - " + btcPrice);
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                                message.setText("Мне не удалось достать курс BTC сейчас. Попробуй позже.");
                            }
                        }
                        case "/ball" -> {
                            states.put(chatId, State.WAITING_FOR_QUESTION);
                            message.setText("Задай мне вопрос, на который можно ответить Да или Нет, и я загляну в будущее...");
                        }
                        case "/quote" -> {
                            String url = "https://api.animechan.io/v1/quotes/random";
                            Request request = new Request.Builder().url(url).build();
                            try (Response response = client.newCall(request).execute()){
                                String jsonResponse = response.body().string();
                                if (response.code() == 200) {
                                    JSONObject obj = new JSONObject(jsonResponse);

                                    JSONObject data = obj.getJSONObject("data");
                                    JSONObject anime = data.getJSONObject("anime");
                                    JSONObject character = data.getJSONObject("character");

                                    String name = anime.getString("name");
                                    String charName = character.getString("name");

                                    String quote = data.getString("content");
                                    message.setText("Цитата: " + quote + "\nНазвание аниме: " + name + "\nИмя персонажа: " + charName);
                                } else {
                                    message.setText("Что-то пошло не так... Попробуй позже.");
                                }
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                                message.setText("Что-то пошло не так... Попробуй позже.");
                            }
                        }
                        case "Кто ты?" -> {
                            message.setText("Я просто глупый робот. А как тебя зовут, человек?");
                            states.put(chatId, State.WAITING_FOR_NAME);
                        }
                        case "/weather" -> {
                            states.put(chatId, State.WAITING_FOR_WEATHER_CITY);
                            message.setText("Напиши название города на английском (например, London или Moscow):");
                        }
                        case "/reverse" -> {
                            states.put(chatId, State.WAITING_FOR_REVERSE);
                            message.setText("Напиши мне любое слово или фразу, и я разверну ее задом наперед!");
                        }
                        case "/rps" -> {
                            states.put(chatId, State.WAITING_FOR_HUMAN_CHOICE);
                            message.setText("Давай сыграем в Камень, Ножницы, Бумага. Выбери свой ход (1-3): \n" +
                                    "1) Камень\n" +
                                    "2) Ножницы\n" +
                                    "3) Бумага\n\n" +
                                    "Проверить статистику можно командой /stats");
                        }
                        case "/stats" -> {
                            message.setText("Твои победы: " + currentUserWins + "| Мои победы: " + currentUserloses);
                        }
                        case "/clear" -> {
                            taskService.clearAllTasks(chatId);
                            message.setText("Задачи удаленны");
                        }
                        default -> message.setText("Я не понимаю. Напиши /help");
                    }
                }
            } else if (currentState == State.WAITING_FOR_NAME) {
                usersNames.put(chatId, text);
                message.setText("Тебя действительно зовут " + text + "? Напиши Да или Нет.");
                states.put(chatId, State.WAITING_FOR_CONFIRM);
            } else if (currentState == State.WAITING_FOR_CONFIRM) {
                if (text.equalsIgnoreCase("Да")) {
                    message.setText("Приятно познакомиться, " + currentUserName + "!");
                } else {
                    message.setText("Извини, я перегрелся. Давай заново.");
                }
                states.put(chatId, State.IDLE);
            } else if (currentState==State.WAITING_FOR_GUESS) {
                try {
                    if (numberToGuess.get(chatId) > Integer.parseInt(text)) {
                        message.setText("Мое число больше! ");
                    } else if (numberToGuess.get(chatId) < Integer.parseInt(text)) {
                        message.setText("Мое число меньше! ");
                    } else {
                        message.setText("Угадал!");

                        states.put(chatId, State.IDLE);
                    }
                } catch (NumberFormatException e) {
                    message.setText("Пожалуйста, введи число цифрами!");
                }
            } else if (currentState== State.WAITING_FOR_REVERSE) {
                char[] letters = text.toCharArray();
                for (int i = 0; i < letters.length / 2; i++) {
                    char temp = letters[i];
                    letters[i] = letters[letters.length - 1 - i];
                    letters[letters.length - 1 - i] = temp;
                }
                String reversedText = new String(letters);
                message.setText(reversedText);
                states.put(chatId, State.IDLE);
            } else if (currentState==  State.WAITING_FOR_HUMAN_CHOICE) {
                try {
                    int humanChoice = Integer.parseInt(text);
                    int computerChoice = rand.nextInt(1, 3 + 1);
                    switch (humanChoice) {
                        case (1) -> {
                            if (computerChoice == 2) {
                                message.setText("Я выбрал ножницы, Ты победил!");
                                rpsHumanGameStats.put(chatId, ++currentUserWins);
                                states.put(chatId, State.IDLE);
                            } else if (computerChoice == 3) {
                                message.setText("Я выбрал бумагу, Ты проиграл!");
                                rpsComputerGameStats.put(chatId, ++currentUserloses);
                                states.put(chatId, State.IDLE);
                            } else {
                                message.setText("Ничья!");
                                states.put(chatId, State.IDLE);
                            }
                        }
                        case (2) -> {
                            if (computerChoice == 1) {
                                message.setText("Я выбрал камень, Ты проиграл!");
                                rpsComputerGameStats.put(chatId, ++currentUserloses);
                                states.put(chatId, State.IDLE);
                            } else if (computerChoice == 3) {
                                message.setText("Я выбрал бумагу, Ты победил!");
                                rpsHumanGameStats.put(chatId, ++currentUserWins);
                                states.put(chatId, State.IDLE);
                            } else {
                                message.setText("Ничья!");
                                states.put(chatId, State.IDLE);
                            }
                        }
                        case (3) -> {
                            if (computerChoice == 1) {
                                message.setText("Я выбрал камень, Ты победил!");
                                rpsHumanGameStats.put(chatId, ++currentUserWins);
                                states.put(chatId, State.IDLE);
                            } else if (computerChoice == 2) {
                                message.setText("Я выбрал ножницы, Ты проиграл!");
                                rpsComputerGameStats.put(chatId, ++currentUserloses);
                                states.put(chatId, State.IDLE);
                            } else {
                                message.setText("Ничья!");
                                states.put(chatId, State.IDLE);
                            }
                        }
                        default -> {
                            message.setText("Я не понимаю введи /help");
                            states.put(chatId, State.IDLE);
                        }
                    }
                } catch (NumberFormatException e) {
                    message.setText("Пожалуйста, введи только цифру хода (1, 2 или 3)!");
                }
            } else if (currentState== State.WAITING_FOR_WEATHER_CITY) {

                String url = "https://api.openweathermap.org/data/2.5/weather?q=" + text + "&appid=" + weatherApiKey + "&units=metric";
                Request request = new Request.Builder().url(url).build();
                try (Response response = client.newCall(request).execute()){
                    String jsonResponse = response.body().string();
                    if (response.code() == 200) {
                        JSONObject obj = new JSONObject(jsonResponse);
                        JSONObject main = obj.getJSONObject("main");
                        double temp = main.getDouble("temp");
                        double feels_like_temp = main.getDouble("feels_like");
                        String cityName = obj.getString("name");
                        message.setText("В городе " + cityName + " сейчас " + temp + "°C" +
                                " Ощущается как " + feels_like_temp);

                    } else if (response.code() == 401) {
                        message.setText("Мой ключ погоды еще не активировался. Подожди немного!");
                    } else if (response.code() == 404) {
                        message.setText("Город не найден. Проверь английское название!");
                    } else {
                        message.setText("Что-то пошло не так при запросе погоды...");
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    message.setText("Что-то пошло не так... Попробуй позже.");
                } finally {
                    states.put(chatId, State.IDLE);
                }


            } else if (currentState== State.WAITING_FOR_QUESTION) {
                String[] answers = {"Бесспорно", "Даже не думай", "Мне кажется — да", "Пока не яснo", "Мой ответ — нет"};
                message.setText(answers[rand.nextInt(0, answers.length)]);
                states.put(chatId, State.IDLE);
            }
            // 4. Оборачиваем отправку в защиту от ошибок сети
            try {
                telegramClient.execute(message);
            } catch (Exception e) {
                e.printStackTrace();
                message.setText("Что-то пошло не так...");
            }
        }
    }
}

