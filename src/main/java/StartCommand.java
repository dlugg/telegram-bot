public class StartCommand implements Command{
    @Override
    public String execute(long chatId, String args){
        return "Привет, я родился!";
    }
}
