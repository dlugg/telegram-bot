package commands;


import service.TaskService;

public class AddCommand implements Command{
    private final TaskService taskService;
    public AddCommand(TaskService taskService){
        this.taskService = taskService;
    }
    @Override
    public String execute(long chatId, String args){
            if (args.isBlank()) {
               return "Напиши задачу после команды (/add твоя задача)";
            } else {
                taskService.addTask(chatId, args);
                return "Задача добавлена";
            }
    }
    @Override
    public String description(){
        return "добавить задачу";
    }

}
