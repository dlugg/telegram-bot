package commands;

import model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskFormatterTest {
    @Test
    void formatsUndoneTaskWithNumber() {
        Task task = new Task("buy bread", false);

        String result = TaskFormatter.format(1, task);

        assertEquals("1. buy bread", result);
    }

    @Test
    void addsCheckmarkToDoneTask() {
        Task task = new Task("study java", true);

        String result = TaskFormatter.format(1, task);

        assertEquals("1. ✅ study java", result);
    }

}
