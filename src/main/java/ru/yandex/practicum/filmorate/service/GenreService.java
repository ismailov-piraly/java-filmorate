package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Service
public class GenreService {

    private final GenreStorage genreStorage;
    private final GenreMapper genreMapper;

    public GenreService(
            @Qualifier("genreDbStorage") GenreStorage genreStorage,
            GenreMapper genreMapper) {

        this.genreStorage = genreStorage;
        this.genreMapper = genreMapper;
    }

    public List<GenreDto> getGenres() {
        return genreStorage.findAll()
                .stream()
                .map(genreMapper::toDto)
                .toList();
    }

    public GenreDto getGenreById(Integer id) {
        Genre genre = genreStorage.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Жанр не найден: " + id));

        return genreMapper.toDto(genre);
    }
}