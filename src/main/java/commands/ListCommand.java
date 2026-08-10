package commands;

import service.TaskService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ListCommand implements Command {
    private final TaskService taskService;

    public ListCommand(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String execute(long chatId, String args) {
        List<String> currentTasks = taskService.getTasks(chatId);
        if (currentTasks.isEmpty()) {
            return "Список задач пуст";
        }
        String result = "Текущие задачи: \n" + IntStream.range(0, currentTasks.size())
                .mapToObj(i -> i + 1 + ". " + currentTasks.get(i))
                .collect(Collectors.joining("\n"));
        return result;

    }

    @Override
    public String description() {
        return "показать список задач";
    }
}