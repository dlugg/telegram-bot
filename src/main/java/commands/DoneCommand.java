package commands;

import service.TaskService;

public class DoneCommand implements Command{
    private final TaskService taskService;
    public DoneCommand(TaskService taskService){
        this.taskService = taskService;
    }
    @Override
    public String execute(long chatId, String args){
            try {
                int index = Integer.parseInt(args)-  1;
                if (taskService.removeTask(chatId, index)) {
                    return("Задача успешно удалена.");
                } else {
                  return "Мне не удалось удалить эту задачу. Попробуй еще раз.";
                }
            } catch (NumberFormatException nfe) {
                return "Введи номер задачи цифрой. ";
            }

        }
    @Override
    public String description(){
        return "отметить задачу выполненной";
    }

}
