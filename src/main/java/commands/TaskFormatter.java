package commands;

import model.Task;

public class TaskFormatter {
    public static String format(int number, Task task) {
        String mark = task.isDone() ? "✅ " : "";
        return number + ". " + mark + task.getText();
    }
}
