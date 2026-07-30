import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
    private Map<Long, String> states = new HashMap<>();        // тут мы сохраним имя юзера
    private Map<Long, String> usersNames = new HashMap<>();
    private Map<Long, Integer> numberToGuess = new HashMap<>();
    private Map<Long, Integer> rpsHumanGameStats = new HashMap<>();
    private Map<Long, Integer> rpsComputerGameStats = new HashMap<>();
    private Map<Long, List<String>> toDoList = new HashMap<>();
    private final String weatherApiKey;
    private final Random rand = new Random();
    private final Map<String, Command> commands = new HashMap<>();

    // Конструктор: при создании бота мы передаем ему токен
    public MyBot(String botToken, String weatherApiKey) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.weatherApiKey = weatherApiKey;
        commands.put("/start", new StartCommand());
        commands.put("/help", new HelpCommand());
        commands.put("/todo", new ToDoCommand());
    }

    private void addTask(Long userId, String text) {
        toDoList.computeIfAbsent(userId, k -> new ArrayList<>()).add(text);
    }

    private List<String> getTasks(Long userId) {
        return new ArrayList<>(toDoList.getOrDefault(userId, List.of()));
    }

    private boolean removeTask(Long userId, int index) {
        if (toDoList.containsKey(userId)) {
            if (index > toDoList.get(userId).size() - 1 || index < 0) {
                return false;
            } else {
                List<String> existing = toDoList.get(userId);
                existing.remove(index);
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            // 1. Вытаскиваем текст, который написал юзер
            String text = update.getMessage().getText();

            // 2. Вытаскиваем ID чата (чтобы бот знал, куда отвечать)
            long chatId = update.getMessage().getChatId();
            String currentState = states.getOrDefault(chatId, "idle");
            String currentUserName = usersNames.getOrDefault(chatId, "");
            int currentUserWins = rpsHumanGameStats.getOrDefault(chatId, 0);
            int currentUserloses = rpsComputerGameStats.getOrDefault(chatId, 0);
            SendMessage message = new SendMessage(String.valueOf(chatId), "");

            if (currentState.equals("idle")) {
                Command cmd = commands.get(text);
                if (cmd != null) {
                    message.setText(cmd.execute());
                } else {
                    if (text.startsWith("/add")) {
                        String[] parts = text.split(" ", 2);
                        if (parts.length < 2) {
                            message.setText("Напиши задачу после команды (/add твоя задача)");
                        } else {
                            addTask(chatId, parts[1]);
                            message.setText("Задача добавлена");
                        }
                    } else if (text.startsWith("/done ")) {
                        String[] parts = text.split(" ", 2);
                        try {
                            int index = Integer.parseInt(parts[1]) - 1;
                            if (removeTask(chatId, index)) {
                                message.setText("Задача успешно удалена.");
                            } else {
                                message.setText("Мне не удалось удалить эту задачу. Попробуй еще раз.");
                            }
                        } catch (NumberFormatException nfe) {
                            message.setText("Введи номер задачи цифрой. ");
                        }

                    } else if (text.startsWith("/remind ")) {
                        String[] parts = text.split(" ", 3);
                        if (parts.length < 3) {
                            message.setText("Проверь написание команды. ");
                        } else {
                            try {
                                int millis = Integer.parseInt(parts[1]) * 1000;
                                if (millis < 0) {
                                    message.setText("Укажи положительное время. ");
                                } else {
                                    message.setText("Я принял твое напоминание! Напомню через " + parts[1] + " секунд.");
                                    new Thread(() -> {
                                        try {
                                            Thread.sleep(millis);
                                            SendMessage remind = new SendMessage(String.valueOf(chatId), parts[2]);
                                            telegramClient.execute(remind);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }).start();
                                }
                            } catch (NumberFormatException e) {
                                message.setText("Проверь написание команды. ");
                            }
                        }
                    } else {
                        switch (text) {
                            case "/guess" -> {
                                numberToGuess.put(chatId, rand.nextInt(1, 10 + 1));
                                states.put(chatId, "waiting_for_guess");
                                message.setText("Я загадал число от 1 до 10. Отгадывай!");
                            }
                            case "/btc" -> {
                                try {
                                    String url = "https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT";
                                    OkHttpClient client = new OkHttpClient();
                                    Request request = new Request.Builder().url(url).build();
                                    Response response = client.newCall(request).execute();
                                    String jsonResponse = response.body().string();
                                    JSONObject obj = new JSONObject(jsonResponse);
                                    double btcPrice = obj.getDouble("price");
                                    message.setText("Текущая цена BTC/USD составляет - " + btcPrice);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            case "/ball" -> {
                                states.put(chatId, "waiting_for_question");
                                message.setText("Задай мне вопрос, на который можно ответить Да или Нет, и я загляну в будущее...");
                            }
                            case "/quote" -> {
                                try {
                                    String url = "https://api.animechan.io/v1/quotes/random";
                                    OkHttpClient client = new OkHttpClient();
                                    Request request = new Request.Builder().url(url).build();
                                    Response response = client.newCall(request).execute();
                                    String jsonResponse = response.body().string();
                                    if (response.code() == 200) {
                                        JSONObject obj = new JSONObject(jsonResponse);

                                        JSONObject data = obj.getJSONObject("data");
                                        JSONObject anime = data.getJSONObject("anime");
                                        JSONObject character = data.getJSONObject("character");

                                        String name = anime.getString("name");
                                        String altName = anime.getString("altName");
                                        String charName = character.getString("name");

                                        String quote = data.getString("content");
                                        message.setText("Цитата: " + quote + "\nНазвание аниме: " + name + "\nИмя персонажа: " + charName);
                                    } else {
                                        message.setText("Попробуй позже.");
                                    }
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            case "Кто ты?" -> {
                                message.setText("Я просто глупый робот. А как тебя зовут, человек?");
                                states.put(chatId, "waiting_for_name");
                            }
                            case "/weather" -> {
                                states.put(chatId, "waiting_for_weather_city");
                                message.setText("Напиши название города на английском (например, London или Moscow):");
                            }
                            case "/reverse" -> {
                                states.put(chatId, "waiting_for_reverse");
                                message.setText("Напиши мне любое слово или фразу, и я разверну ее задом наперед!");
                            }
                            case "/rps" -> {
                                states.put(chatId, "waiting_for_human_choice");
                                message.setText("Давай сыграем в Камень, Ножницы, Бумага. Выбери свой ход (1-3): \n" +
                                        "1) Камень\n" +
                                        "2) Ножницы\n" +
                                        "3) Бумага\n\n" +
                                        "Проверить статистику можно командой /stats");
                            }
                            case "/stats" -> {
                                message.setText("Твои победы: " + currentUserWins + "| Мои победы: " + currentUserloses);
                            }
                            case "/list" -> {
                                List<String> currentTasks = getTasks(chatId);
                                StringBuilder currentTasksOutput = new StringBuilder();
                                if (currentTasks.isEmpty()) {
                                    message.setText("Задачи отсутствуют ");
                                } else {
                                    currentTasksOutput.append("Текущие задачи: \n");
                                    for (int i = 0; i < currentTasks.size(); i++) {
                                        currentTasksOutput.append(i + 1).append(". ").append(currentTasks.get(i)).append("\n");

                                    }
                                    message.setText(currentTasksOutput.toString());
                                }
                            }
                            case "/clear" -> {
                                toDoList.remove(chatId);
                                message.setText("Задачи удаленны");
                            }
                            default -> message.setText("Я не понимаю. Напиши /help");
                        }
                    }
                }
            } else if (currentState.equals("waiting_for_name")) {
                usersNames.put(chatId, text);
                message.setText("Тебя действительно зовут " + text + "? Напиши Да или Нет.");
                states.put(chatId, "waiting_for_confirm");
            } else if (currentState.equals("waiting_for_confirm")) {
                if (text.equalsIgnoreCase("Да")) {
                    message.setText("Приятно познакомиться, " + currentUserName + "!");
                } else {
                    message.setText("Извини, я перегрелся. Давай заново.");
                }
                states.put(chatId, "idle");
            } else if (currentState.equals("waiting_for_guess")) {
                try {
                    if (numberToGuess.get(chatId) > Integer.parseInt(text)) {
                        message.setText("Мое число больше! ");
                    } else if (numberToGuess.get(chatId) < Integer.parseInt(text)) {
                        message.setText("Мое число меньше! ");
                    } else {
                        message.setText("Угадал!");

                        states.put(chatId, "idle");
                    }
                } catch (NumberFormatException e) {
                    message.setText("Пожалуйста, введи число цифрами!");
                }
            } else if (currentState.equals("waiting_for_reverse")) {
                char[] letters = text.toCharArray();
                for (int i = 0; i < letters.length / 2; i++) {
                    char temp = letters[i];
                    letters[i] = letters[letters.length - 1 - i];
                    letters[letters.length - 1 - i] = temp;
                }
                String reversedText = new String(letters);
                message.setText(reversedText);
                states.put(chatId, "idle");
            } else if (currentState.equals("waiting_for_human_choice")) {
                try {
                    int humanChoice = Integer.parseInt(text);
                    int computerChoice = rand.nextInt(1, 3 + 1);
                    switch (humanChoice) {
                        case (1) -> {
                            if (computerChoice == 2) {
                                message.setText("Я выбрал ножницы, Ты победил!");
                                rpsHumanGameStats.put(chatId, ++currentUserWins);
                                states.put(chatId, "idle");
                            } else if (computerChoice == 3) {
                                message.setText("Я выбрал бумагу, Ты проиграл!");
                                rpsComputerGameStats.put(chatId, ++currentUserloses);
                                states.put(chatId, "idle");
                            } else {
                                message.setText("Ничья!");
                                states.put(chatId, "idle");
                            }
                        }
                        case (2) -> {
                            if (computerChoice == 1) {
                                message.setText("Я выбрал камень, Ты проиграл!");
                                rpsComputerGameStats.put(chatId, ++currentUserloses);
                                states.put(chatId, "idle");
                            } else if (computerChoice == 3) {
                                message.setText("Я выбрал бумагу, Ты победил!");
                                rpsHumanGameStats.put(chatId, ++currentUserWins);
                                states.put(chatId, "idle");
                            } else {
                                message.setText("Ничья!");
                                states.put(chatId, "idle");
                            }
                        }
                        case (3) -> {
                            if (computerChoice == 1) {
                                message.setText("Я выбрал камень, Ты победил!");
                                rpsHumanGameStats.put(chatId, ++currentUserWins);
                                states.put(chatId, "idle");
                            } else if (computerChoice == 2) {
                                message.setText("Я выбрал ножницы, Ты проиграл!");
                                rpsComputerGameStats.put(chatId, ++currentUserloses);
                                states.put(chatId, "idle");
                            } else {
                                message.setText("Ничья!");
                                states.put(chatId, "idle");
                            }
                        }
                        default -> {
                            message.setText("Я не понимаю введи /help");
                            states.put(chatId, "idle");
                        }
                    }
                } catch (NumberFormatException e) {
                    message.setText("Пожалуйста, введи только цифру хода (1, 2 или 3)!");
                }
            } else if (currentState.equals("waiting_for_weather_city")) {

                String url = "https://api.openweathermap.org/data/2.5/weather?q=" + text + "&appid=" + weatherApiKey + "&units=metric";
                System.out.println("Собранный URL: " + url);
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(url).build();
                    Response response = client.newCall(request).execute();
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
                    System.out.println(jsonResponse);
                    states.put(chatId, "idle");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }


            } else if (currentState.equals("waiting_for_question")) {
                String[] answers = {"Бесспорно", "Даже не думай", "Мне кажется — да", "Пока не яснo", "Мой ответ — нет"};
                message.setText(answers[rand.nextInt(0, answers.length)]);
                states.put(chatId, "idle");
            }
            // 4. Оборачиваем отправку в защиту от ошибок сети
            try {
                telegramClient.execute(message); // Почтальон, отправляй!
            } catch (
                    Exception e) {
                e.printStackTrace(); // Если ошибка - выведет в консоль
            }
        }
    }
}


