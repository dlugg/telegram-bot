import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {
    public static void main(String[] args) {
        // 1. Положили токен в переменную
        String botToken = System.getenv("BOT_TOKEN");
        if (botToken == null) {
            throw new IllegalStateException("BOT_TOKEN не задан");
        }
        final String weatherApiKey = System.getenv("WEATHER_API");
        if (weatherApiKey == null) {
            throw new IllegalStateException("WEATHER_API не задан");
        }
        try {
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();

            // 2. Используем эту переменную для регистрации и для создания бота
            botsApplication.registerBot(botToken, new MyBot(botToken, weatherApiKey));

            System.out.println("Бот успешно запущен!");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
