package commands;

import service.TaskService;

public class ClearCommand implements Command {
    private final TaskService taskService;

    public ClearCommand(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String execute(long chatId, String args) {

        int deleted = taskService.clearAllTasks(chatId);
        if (deleted == 0) {
            return "Список задач и так пуст";
        } else {
            return "Удалено задач: " + deleted;
        }

    }

    @Override
    public String description() {
        return "очистить весь список задач";
    }
}
