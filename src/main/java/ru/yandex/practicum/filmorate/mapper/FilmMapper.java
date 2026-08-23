package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class FilmMapper {

    public Film toEntity(FilmDto dto) {
        Film film = new Film();

        film.setId(dto.getId());
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());

        if (dto.getMpa() != null) {
            Mpa mpa = new Mpa();
            mpa.setId(dto.getMpa().getId());
            mpa.setName(dto.getMpa().getName());

            film.setMpa(mpa);
        }

        if (dto.getGenres() != null) {
            film.setGenres(
                    dto.getGenres()
                            .stream()
                            .map(this::toGenreEntity)
                            .collect(Collectors.toSet())
            );
        }

        return film;
    }

    public FilmDto toDto(Film film) {
        FilmDto dto = new FilmDto();

        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        if (film.getMpa() != null) {
            MpaDto mpaDto = new MpaDto();

            mpaDto.setId(film.getMpa().getId());
            mpaDto.setName(film.getMpa().getName());

            dto.setMpa(mpaDto);
        }

        if (film.getGenres() != null) {
            dto.setGenres(
                    film.getGenres()
                            .stream()
                            .map(this::toGenreDto)
                            .collect(Collectors.toSet())
            );
        } else {
            dto.setGenres(new HashSet<>());
        }

        return dto;
    }

    private Genre toGenreEntity(GenreDto dto) {
        Genre genre = new Genre();

        genre.setId(dto.getId());
        genre.setName(dto.getName());

        return genre;
    }

    private GenreDto toGenreDto(Genre genre) {
        GenreDto dto = new GenreDto();

        dto.setId(genre.getId());
        dto.setName(genre.getName());

        return dto;
    }
}