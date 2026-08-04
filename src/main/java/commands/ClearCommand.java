package commands;

import service.TaskService;

public class ClearCommand implements Command{
    private final TaskService taskService;
    public ClearCommand(TaskService taskService){
        this.taskService= taskService;
    }
    @Override
    public String execute(long chatId, String args) {
        taskService.clearAllTasks(chatId);
        return "Задачи удаленны";
    }
}
