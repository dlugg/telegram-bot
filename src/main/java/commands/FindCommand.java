package commands;

import model.Task;
import service.TaskService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static commands.TaskFormatter.format;

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
            List<Task> tasks = taskService.getTasks(chatId);
            String result  = IntStream.range(0,tasks.size())
                    .filter(i -> tasks.get(i).getText().toLowerCase().contains(wordIgnoreCase))
                    .mapToObj(i -> format(i+1, tasks.get(i)))
                    .collect(Collectors.joining("\n"));

            if (result.isEmpty()) {
                return "Задачи не найдены";
            } else {
                return result;
            }
        }
    }

    @Override
    public String description() {
        return "найти задачи по слову";
    }
}
