package commands;

import java.util.Map;
import java.util.stream.Collectors;

public class HelpCommand implements Command {
    private final Map<String, Command> commands;
    public HelpCommand(Map<String, Command> commandMap){
        this.commands = commandMap;
    }
    @Override
    public String execute(long chatId, String args) {
        return commands.entrySet().stream()
                .map(e-> e.getKey() + " " + e.getValue().description())
                .collect(Collectors.joining("\n"));

    }
    @Override
    public String description(){
        return "список команд";
    }
}
