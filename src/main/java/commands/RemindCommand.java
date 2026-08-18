package commands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class RemindCommand implements Command {
    private final TelegramClient telegramClient;
private static final int TIME_LIMIT_IN_MINUTES = 360;
    public RemindCommand(TelegramClient telegramClient) {

        this.telegramClient = telegramClient;
    }

    @Override
    public String execute(long chatId, String args) {

        String[] partsRemind = args.split(" ", 2);
        if (partsRemind.length < 2) {
            return "Проверь написание команды. ";
        } else {
            try {
                int userTimeInMinutes = Integer.parseInt(partsRemind[0]);
                if (userTimeInMinutes > TIME_LIMIT_IN_MINUTES) {
                    return "Я могу напомнить тебе в пределах " + TIME_LIMIT_IN_MINUTES + " минут";
                } else if (userTimeInMinutes < 0) {
                    return "Укажи положительное время. ";
                }
                long millis = userTimeInMinutes * 60_000L;
                new Thread(() -> {
                    try {
                        Thread.sleep(millis);
                        SendMessage remind = new SendMessage(String.valueOf(chatId), partsRemind[1]);
                        telegramClient.execute(remind);
                    } catch (InterruptedException e) {
                        try {
                            telegramClient.execute(new SendMessage(String.valueOf(chatId), "Что то пошло не так..."));
                        } catch (TelegramApiException ex) {
                            ex.printStackTrace();
                        }
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }).start();

                return "Я принял твое напоминание! Напомню через " + userTimeInMinutes + " минут.";

            } catch (NumberFormatException e) {
                return "Проверь написание команды. ";
            }
        }
    }

    @Override
    public String description() {
        return "напоминалка";
    }
}
