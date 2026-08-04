package bot;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {
    public static void main(String[] args)throws Exception {
        // 1. Положили токен в переменную
        String botToken = System.getenv("BOT_TOKEN");
        if (botToken == null) {
            throw new IllegalStateException("BOT_TOKEN не задан");
        }
        final String weatherApiKey = System.getenv("WEATHER_API_KEY");
        if (weatherApiKey == null) {
            throw new IllegalStateException("WEATHER_API_KEY не задан");
        }

            try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()){

            botsApplication.registerBot(botToken, new MyBot(botToken, weatherApiKey));

            System.out.println("Бот успешно запущен!");
            Thread.currentThread().join();}


    }
}
