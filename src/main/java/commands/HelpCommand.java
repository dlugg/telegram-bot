package commands;

import java.util.Map;

public class HelpCommand implements Command {
    private final Map<String, Command> commands;
    public HelpCommand(Map<String, Command> commandMap){
        this.commands = commandMap;
    }
    @Override
    public String execute(long chatId, String args) {
        StringBuilder output = new StringBuilder();
        for (Map.Entry<String, Command> entry : commands.entrySet()){
            output.append(entry.getKey()).append(" ").append(entry.getValue().description()).append("\n");
        }
        return output.toString();

    }
    @Override
    public String description(){
        return "список команд";
    }
}
