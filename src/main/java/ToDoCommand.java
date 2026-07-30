public class ToDoCommand implements Command {
    @Override
    public String execute() {
        return """
                Я могу сделать список задач! 
                /add  — добавить дело в список.
                /list — показать все мои дела.
                /clear — очистить список.""";
    }

}
