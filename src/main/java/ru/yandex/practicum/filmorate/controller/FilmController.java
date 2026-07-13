package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;

    private Long getNextId() {
        return nextId++;
    }

    @PostMapping
    public Film create(@RequestBody Film film) {

        validateFilm(film);

        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Добавлен фильм: {}", film);

        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {

        if (film.getId() == null) {
            log.warn("Не указан id фильма");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с id={} не найден", film.getId());
            throw new NotFoundException("Фильм не найден");
        }

        validateFilm(film);

        films.put(film.getId(), film);

        log.info("Обновлен фильм: {}", film);

        return film;
    }

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    private void validateFilm(Film film) {

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Пустое название фильма");
            throw new ConditionsNotMetException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null
                && film.getDescription().length() > 200) {
            log.warn("Описание фильма превышает 200 символов");
            throw new ConditionsNotMetException("Описание фильма не может быть длиннее 200 символов");
        }

        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            log.warn("Некорректная дата релиза");
            throw new ConditionsNotMetException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.warn("Некорректная продолжительность фильма");
            throw new ConditionsNotMetException("Продолжительность фильма должна быть положительной");
        }
    }
}