package commands;

import service.TaskService;

import java.util.stream.Collectors;

public class FindCommand implements Command {
    private final TaskService taskService;

    public FindCommand(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String execute(long chatId, String args) {
        if (args.isBlank()) {
            return "Напиши слово которое содержат задачи";

        } else {
            String wordIgnoreCase = args.toLowerCase();
            String result =  taskService.getTasks(chatId).stream()
                    .filter(t -> t.toLowerCase().contains(wordIgnoreCase))
                    .collect(Collectors.joining("\n"));
            if (result.isEmpty()){
                return "Задачи не найдены";
            }else {
                return result;
            }
        }
    }
    @Override
    public String description(){
        return "найти задачи по слову";
    }
}
