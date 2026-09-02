package commands;

import service.TaskService;

public class MarkAsDoneCommand implements Command {
    private final TaskService taskService;

    public MarkAsDoneCommand(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String execute(long chatId, String args) {
        try {
            int position = Integer.parseInt(args);
            if (taskService.markAsDone(chatId, position)) {
                return "Задача успешно отмечена выполненной";
            } else {
                return "Нет задач с таким номером";
            }
        } catch (NumberFormatException e) {
            return "Введи номер задачи цифрой";
        }
    }

}
