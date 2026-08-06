package commands;

public interface Command {
    String execute(long chatId, String args);

    default String description() {
        return "описание не задано";
    }
}