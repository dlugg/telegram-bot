import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class RemindCommand implements Command {
    private final TelegramClient telegramClient;

    RemindCommand(TelegramClient telegramClient) {

        this.telegramClient = telegramClient;
    }

    @Override
    public String execute(long chatId, String args) {

        String[] partsRemind = args.split(" ", 2);
        if (partsRemind.length < 2) {
            return "Проверь написание команды. ";
        } else {
            try {
                int millis = Integer.parseInt(partsRemind[0]) * 1000;
                if (millis < 0) {
                    return "Укажи положительное время. ";
                } else {
                    new Thread(() -> {
                        try {
                            Thread.sleep(millis);
                            SendMessage remind = new SendMessage(String.valueOf(chatId), partsRemind[1]);
                            telegramClient.execute(remind);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
                return "Я принял твое напоминание! Напомню через " + partsRemind[0] + " секунд.";

            } catch (NumberFormatException e) {
                return "Проверь написание команды. ";
            }
        }
    }
}
