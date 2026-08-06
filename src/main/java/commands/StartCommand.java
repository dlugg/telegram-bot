package commands;

public class StartCommand implements Command {
    @Override
    public String execute(long chatId, String args){
        return "Привет, я родился!";
    }

    @Override
    public String description(){
        return "родить бота";
    }
}
