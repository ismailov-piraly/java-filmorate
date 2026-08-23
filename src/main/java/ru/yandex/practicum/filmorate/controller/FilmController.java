package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    public ResponseEntity<FilmDto> create(@RequestBody FilmDto filmDto) {
        return ResponseEntity.ok(filmService.create(filmDto));
    }

    @PutMapping
    public ResponseEntity<FilmDto> update(@RequestBody FilmDto filmDto) {
        return ResponseEntity.ok(filmService.update(filmDto));
    }

    @GetMapping
    public ResponseEntity<List<FilmDto>> getFilms() {
        return ResponseEntity.ok(filmService.getFilms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDto> getFilmById(@PathVariable Long id) {
        return ResponseEntity.ok(filmService.getFilmById(id));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(
            @PathVariable Long id,
            @PathVariable Long userId) {

        filmService.addLike(id, userId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> deleteLike(
            @PathVariable Long id,
            @PathVariable Long userId) {

        filmService.deleteLike(id, userId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FilmDto>> getPopularFilms(
            @RequestParam(defaultValue = "10") int count) {

        return ResponseEntity.ok(
                filmService.getPopularFilms(count)
        );
    }
}