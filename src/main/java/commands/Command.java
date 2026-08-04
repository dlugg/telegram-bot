package commands;

public interface Command{
    String execute(long chatId, String args);

}