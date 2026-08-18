package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage,
                       UserStorage userStorage) {

        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        validateFilm(film);

        Film createdFilm = filmStorage.create(film);
        log.info("Добавлен фильм {}", createdFilm);

        return createdFilm;
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new ConditionsNotMetException("Id фильма должен быть указан");
        }

        if (filmStorage.findById(film.getId()) == null) {
            throw new NotFoundException("Фильм не найден");
        }

        validateFilm(film);

        Film updatedFilm = filmStorage.update(film);
        log.info("Обновлен фильм {}", updatedFilm);

        return updatedFilm;
    }

    public List<Film> getFilms() {
        return filmStorage.findAll();
    }

    public Film getFilmById(Long id) {
        Film film = filmStorage.findById(id);

        if (film == null) {
            throw new NotFoundException("Фильм не найден");
        }

        return film;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ConditionsNotMetException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ConditionsNotMetException("Описание фильма не должно превышать 200 символов");
        }

        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            throw new ConditionsNotMetException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            throw new ConditionsNotMetException("Продолжительность фильма должна быть положительной");
        }
    }

    public void addLike(Long filmId, Long userId) {

        Film film = getFilmById(filmId);

        if (userStorage.findById(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        film.getLikes().add(userId);

        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {

        Film film = getFilmById(filmId);

        if (userStorage.findById(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }

        film.getLikes().remove(userId);

        log.info("Пользователь {} удалил лайк фильму {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {

        return filmStorage.findAll()
                .stream()
                .sorted((f1, f2) ->
                        Integer.compare(
                                f2.getLikes().size(),
                                f1.getLikes().size()))
                .limit(count)
                .toList();
    }


}