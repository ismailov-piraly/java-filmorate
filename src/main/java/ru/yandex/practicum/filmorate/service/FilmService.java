package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final FilmMapper filmMapper;

    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            @Qualifier("mpaDbStorage") MpaStorage mpaStorage,
            @Qualifier("genreDbStorage") GenreStorage genreStorage,
            FilmMapper filmMapper) {

        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.filmMapper = filmMapper;
    }

    public FilmDto create(FilmDto filmDto) {
        Film film = filmMapper.toEntity(filmDto);

        validateFilm(film);

        Film createdFilm = filmStorage.create(film);

        log.info("Добавлен фильм {}", createdFilm);

        return filmMapper.toDto(createdFilm);
    }

    public FilmDto update(FilmDto filmDto) {
        Film film = filmMapper.toEntity(filmDto);

        if (film.getId() == null) {
            throw new ConditionsNotMetException("Id фильма должен быть указан");
        }

        getFilmOrThrow(film.getId());

        validateFilm(film);

        Film updatedFilm = filmStorage.update(film);

        log.info("Обновлён фильм {}", updatedFilm);

        return filmMapper.toDto(updatedFilm);
    }

    public List<FilmDto> getFilms() {
        return filmStorage.findAll()
                .stream()
                .map(filmMapper::toDto)
                .toList();
    }

    public FilmDto getFilmById(Long id) {
        return filmMapper.toDto(getFilmOrThrow(id));
    }

    private void validateFilm(Film film) {

        if (film.getName() == null || film.getName().isBlank()) {
            throw new ConditionsNotMetException(
                    "Название фильма не может быть пустым"
            );
        }

        if (film.getDescription() != null
                && film.getDescription().length() > 200) {

            throw new ConditionsNotMetException(
                    "Описание фильма не должно превышать 200 символов"
            );
        }

        LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            throw new ConditionsNotMetException(
                    "Дата релиза не может быть раньше 28 декабря 1895 года"
            );
        }

        if (film.getDuration() <= 0) {
            throw new ConditionsNotMetException(
                    "Продолжительность фильма должна быть положительной"
            );
        }

        if (film.getMpa() != null) {
            mpaStorage.findById(film.getMpa().getId())
                    .orElseThrow(() ->
                            new NotFoundException(
                                    "Рейтинг MPA не найден: "
                                            + film.getMpa().getId()
                            )
                    );
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreStorage.findById(genre.getId())
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Жанр не найден: " + genre.getId()
                                )
                        );
            }
        }
    }

    private Film getFilmOrThrow(Long filmId) {
        return filmStorage.findById(filmId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Фильм не найден: " + filmId
                        ));
    }

    public void addLike(Long filmId, Long userId) {

        getFilmOrThrow(filmId);

        userStorage.findUserById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Пользователь не найден: " + userId
                        ));

        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {

        getFilmOrThrow(filmId);

        userStorage.findUserById(userId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Пользователь не найден: " + userId
                        ));

        filmStorage.deleteLike(filmId, userId);
    }

    public List<FilmDto> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count)
                .stream()
                .map(filmMapper::toDto)
                .toList();
    }
}