package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {

        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        User createdUser = userStorage.create(user);

        log.info("Добавлен пользователь: {}", createdUser);

        return createdUser;
    }

    public User update(User user) {

        if (user.getId() == null) {
            log.warn("Не указан id пользователя");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (userStorage.findById(user.getId()) == null) {
            log.warn("Пользователь с id={} не найден", user.getId());
            throw new NotFoundException("Пользователь не найден");
        }

        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        User updatedUser = userStorage.update(user);

        log.info("Обновлен пользователь: {}", updatedUser);

        return updatedUser;
    }

    public List<User> getUsers() {
        return userStorage.findAll();
    }

    public User getUserById(Long id) {

        User user = userStorage.findById(id);

        if (user == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        return user;
    }

    private void validateUser(User user) {

        if (user.getEmail() == null
                || user.getEmail().isBlank()
                || !user.getEmail().contains("@")) {

            log.warn("Некорректный email");

            throw new ConditionsNotMetException("Электронная почта указана неверно");
        }

        if (user.getLogin() == null
                || user.getLogin().isBlank()
                || user.getLogin().contains(" ")) {

            log.warn("Некорректный логин");

            throw new ConditionsNotMetException("Логин не может быть пустым и содержать пробелы");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {

            log.warn("Дата рождения в будущем");

            throw new ConditionsNotMetException("Дата рождения не может быть в будущем");
        }
    }

    public void addFriend(Long userId, Long friendId) {

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        log.info("Пользователь {} удалил из друзей {}", userId, friendId);
    }

    public List<User> getFriends(Long userId) {

        User user = getUserById(userId);

        return user.getFriends()
                .stream()
                .map(userStorage::findById)
                .toList();
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {

        User firstUser = getUserById(userId);
        User secondUser = getUserById(otherId);

        return firstUser.getFriends()
                .stream()
                .filter(secondUser.getFriends()::contains)
                .map(userStorage::findById)
                .toList();
    }
}