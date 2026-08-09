package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskService {
    private final Map<Long, List<String>> toDoList = new HashMap<>();

    public void addTask(Long userId, String text) {
        toDoList.computeIfAbsent(userId, k -> new ArrayList<>()).add(text);
    }

    public List<String> getTasks(Long userId) {
        return new ArrayList<>(toDoList.getOrDefault(userId, List.of()));
    }

    public boolean removeTask(Long userId, int index) {
        if (toDoList.containsKey(userId)) {
            if (index > toDoList.get(userId).size() - 1 || index < 0) {
                return false;
            } else {
                List<String> existing = toDoList.get(userId);
                existing.remove(index);
                return true;
            }
        } else {
            return false;
        }
    }

    public String clearAllTasks(    Long userId){
        toDoList.remove(userId);
        return "Задачи удалены";
    }
}
