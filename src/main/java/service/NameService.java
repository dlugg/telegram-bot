package service;

import exception.DataAccessException;
import repository.UserRepository;

import java.sql.SQLException;


public class NameService {
    private final UserRepository userRepository;

    public NameService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getUserName(long chatId) {
        try {
            return userRepository.getUserName(chatId);
        } catch (SQLException e) {
            throw new DataAccessException("не удалось получить имя пользователя", e);
        }
    }

    public boolean saveUserName(long chatId, String name) {
        try {
            return userRepository.saveUserName(chatId, name) > 0;
        } catch (SQLException e) {
            throw new DataAccessException("не удалось сохранить имя пользователя", e);
        }
    }

    public void deleteUserName(long chatId) {
        try {
            userRepository.deleteUserName(chatId);
        } catch (SQLException e) {
            throw new DataAccessException("не удалось удалить имя пользователя", e);
        }
    }
}
