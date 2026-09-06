package repository;

import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskRepositoryTest {
    Database database = new Database("jdbc:postgresql://localhost:5432/javabot_test", "postgres", System.getenv("DATABASE_PASSWORD"));
    UserRepository userRepository = new UserRepository(database);
    TaskRepository taskRepository = new TaskRepository(database, userRepository);

    @BeforeEach
    void clearTasksAndUsersTables() throws SQLException {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("TRUNCATE tasks, users RESTART IDENTITY")) {
            statement.execute();
        }
    }

    @Test
    void addedTaskAppearsInList() throws SQLException {
        long chatId = 123;

        taskRepository.addTask(chatId, "walk a dog");

        List<Task> tasks = taskRepository.getTasks(chatId);
        assertEquals(1, tasks.size());
        assertEquals("walk a dog", tasks.get(0).getText());
        assertFalse(tasks.get(0).isDone());
    }

    @Test
    void returnsEmptyListForUnknownUser() throws SQLException {
        long chatId = 123;

        List<Task> tasksList = taskRepository.getTasks(chatId);

        assertEquals(0, tasksList.size());


    }

    @Test
    void doesNotCreateUserWhenReadingTasks() throws SQLException {
        long chatId = 123;
        taskRepository.getTasks(chatId);
        Long userId = userRepository.findUserId(chatId);

        assertNull(userId);
    }


    @Test
    void clearingTasksDoesNotAffectOtherUsers() throws SQLException {
        long chatIdFirstUser = 123;
        long chatIdSecondUser = 456;

        taskRepository.addTask(chatIdFirstUser, "first user test task");
        taskRepository.addTask(chatIdSecondUser, "second user test task");

        taskRepository.clearAllTasks(chatIdFirstUser);

        assertEquals(1, taskRepository.getTasks(chatIdSecondUser).size());
        assertEquals(0, taskRepository.getTasks(chatIdFirstUser).size());
    }

    @Test
    void deletingTaskKeepsOriginalOrder() throws SQLException {
        long chatId = 123;

        taskRepository.addTask(chatId, "walk a dog");
        taskRepository.addTask(chatId, "read a book");
        taskRepository.addTask(chatId, "eat");

        taskRepository.removeTask(chatId, 2);
        List<Task> tasks = taskRepository.getTasks(chatId);
        assertEquals(2, tasks.size());
        assertEquals("walk a dog", tasks.get(0).getText());
        assertEquals("eat", tasks.get(1).getText());
    }

    @Test
    void deletingTaskByNonExistingPositionReturnsZero() throws SQLException {
        long chatId = 123;
        taskRepository.addTask(chatId, "eat");

        assertEquals(0, taskRepository.removeTask(chatId, 2));
        assertEquals(0, taskRepository.removeTask(chatId, 0));
        assertEquals(0, taskRepository.removeTask(chatId, -1));
    }

    @Test
    void checkmarkingTaskAffectOnlyTheSpecifiedTask() throws SQLException {
        long chatId = 123;

        taskRepository.addTask(chatId, "eat");
        taskRepository.addTask(chatId, "study");
        taskRepository.addTask(chatId, "walk a dog");

        taskRepository.markAsDone(chatId, 2);

        List<Task> tasks = taskRepository.getTasks(chatId);

        assertFalse(tasks.get(0).isDone());
        assertTrue(tasks.get(1).isDone());
        assertFalse(tasks.get(2).isDone());
    }


    @Test
    void checkmarkingTaskByNonExistingPositionReturnsZero() throws SQLException {
        long chatId = 123;

        taskRepository.addTask(chatId, "eat");


        assertEquals(0, taskRepository.markAsDone(chatId, 2));
        assertEquals(0, taskRepository.markAsDone(chatId, 0));
        assertEquals(0, taskRepository.markAsDone(chatId, -1));
    }

    @Test
    void orderInTaskListSameAsInAddingOrder() throws SQLException {
        long chatId = 123;

        taskRepository.addTask(chatId, "eat");
        taskRepository.addTask(chatId, "study");
        taskRepository.addTask(chatId, "walk a dog");
        taskRepository.addTask(chatId, "poop");

        List<Task> tasks = taskRepository.getTasks(chatId);

        assertEquals("eat", tasks.get(0).getText());
        assertEquals("study", tasks.get(1).getText());
        assertEquals("walk a dog", tasks.get(2).getText());
        assertEquals("poop", tasks.get(3).getText());
    }

    @Test
    void clearingTaskForUserWithNoTaskReturnsZero() throws SQLException {
        long chatId = 123;
        assertEquals(0, taskRepository.clearAllTasks(chatId));


    }
}



