package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;
    private final UserMapper userMapper;

    public UserService(
            @Qualifier("userDbStorage") UserStorage userStorage,
            UserMapper userMapper) {

        this.userStorage = userStorage;
        this.userMapper = userMapper;
    }

    public UserDto create(UserDto userDto) {

        User user = userMapper.toEntity(userDto);

        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        User createdUser = userStorage.create(user);

        log.info("Добавлен пользователь: {}", createdUser);

        return userMapper.toDto(createdUser);
    }

    public UserDto update(UserDto userDto) {

        User user = userMapper.toEntity(userDto);

        if (user.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        getUserOrThrow(user.getId());

        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        User updatedUser = userStorage.update(user);

        log.info("Обновлен пользователь: {}", updatedUser);

        return userMapper.toDto(updatedUser);
    }

    public List<UserDto> getUsers() {
        return userStorage.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto getUserById(Long id) {
        return userMapper.toDto(getUserOrThrow(id));
    }

    private void validateUser(User user) {

        if (user.getEmail() == null
                || user.getEmail().isBlank()
                || !user.getEmail().contains("@")) {

            log.warn("Некорректный email");

            throw new ConditionsNotMetException(
                    "Электронная почта указана неверно"
            );
        }

        if (user.getLogin() == null
                || user.getLogin().isBlank()
                || user.getLogin().contains(" ")) {

            log.warn("Некорректный логин");

            throw new ConditionsNotMetException(
                    "Логин не может быть пустым и содержать пробелы"
            );
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {

            log.warn("Дата рождения в будущем");

            throw new ConditionsNotMetException(
                    "Дата рождения не может быть в будущем"
            );
        }
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findUserById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Пользователь не найден: " + userId
                        ));
    }

    public void addFriend(Long userId, Long friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);

        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);

        userStorage.removeFriend(userId, friendId);
    }

    public List<UserDto> getFriends(Long userId) {
        getUserOrThrow(userId);

        return userStorage.findFriends(userId)
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    public List<UserDto> getCommonFriends(Long userId, Long otherId) {
        getUserOrThrow(userId);
        getUserOrThrow(otherId);

        return userStorage.findCommonFriends(userId, otherId)
                .stream()
                .map(userMapper::toDto)
                .toList();
    }
}