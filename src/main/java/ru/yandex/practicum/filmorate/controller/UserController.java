package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    private Long getNextId() {
        return nextId++;
    }

    @PostMapping
    public User create(@RequestBody User user) {

        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        users.put(user.getId(), user);

        log.info("Добавлен пользователь: {}", user);

        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {

        if (user.getId() == null) {
            log.warn("Не указан id пользователя");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (!users.containsKey(user.getId())) {
            log.warn("Пользователь с id={} не найден", user.getId());
            throw new NotFoundException("Пользователь не найден");
        }

        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        users.put(user.getId(), user);

        log.info("Обновлен пользователь: {}", user);

        return user;
    }

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
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
}