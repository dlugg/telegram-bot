package commands;

import service.TaskService;

public class RemoveTaskCommand implements Command {
    private final TaskService taskService;

    public RemoveTaskCommand(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String execute(long chatId, String args) {
        try {
            int position = Integer.parseInt(args);
            if (taskService.removeTask(chatId, position)) {
                return "Задача успешно удалена";
            } else {
                return "Нет задач с таким номером";
            }
        } catch (NumberFormatException nfe) {
            return "Введи номер задачи цифрой. ";
        }

    }

    @Override
    public String description() {
        return "удалить задачу";
    }

}
