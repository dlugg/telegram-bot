package service;

import exception.DataAccessException;
import model.Task;
import repository.TaskRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskService {
    private final TaskRepository taskRepository;
    private final Map<Long, List<String>> toDoList = new HashMap<>();

    public TaskService(TaskRepository taskRepository) {

        this.taskRepository = taskRepository;

    }

    public void addTask(Long userId, String text) {
        try {
            taskRepository.addTask(userId, text);
        } catch (SQLException e) {
            throw new DataAccessException("не удалось добавить задачу", e);
        }
    }

    public List<Task> getTasks(Long userId) {
        List<Task> userTasks = new ArrayList<>();
        try {
            userTasks = taskRepository.getTasks(userId);
        } catch (SQLException e) {
            throw new DataAccessException("не удалось получить список задач", e);
        }
        return userTasks;
    }


    public int clearAllTasks(Long userId) {
        try {
            return taskRepository.clearAllTasks(userId);
        } catch (SQLException e) {
            throw new DataAccessException("не удалось удалить все задачи", e);
        }
    }

    public boolean removeTask(Long userId, int position) {
        try {
            return taskRepository.removeTask(userId, position) > 0;
        } catch (SQLException e) {
            throw new DataAccessException("не удалось удалить задачу по данной позиции или данного пользователя", e);
        }
    }

    public boolean markAsDone(Long userId, int position) {
        try {
            return taskRepository.markAsDone(userId, position) > 0;
        } catch (SQLException e) {
            throw new DataAccessException("не удалось отметить задачу выполненной по данной позиции", e);
        }
    }
}
