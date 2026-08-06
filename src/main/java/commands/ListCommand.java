package commands;

import service.TaskService;

import java.util.List;

public class ListCommand implements Command {
private final TaskService taskService;
    public ListCommand(TaskService taskService){
        this.taskService= taskService;
    }
    @Override
    public String execute(long chatId, String args) {
        List<String> currentTasks = taskService.getTasks(chatId);
        StringBuilder currentTasksOutput = new StringBuilder();
        if (currentTasks.isEmpty()) {
            return "Задачи отсутствуют ";
        } else {
            currentTasksOutput.append("Текущие задачи: \n");
            for (int i = 0; i < currentTasks.size(); i++) {
                currentTasksOutput.append(i + 1).append(". ").append(currentTasks.get(i)).append("\n");

            }
        }
        return currentTasksOutput.toString();
    }

    @Override
    public String description(){
        return "показать список задач";
    }
}