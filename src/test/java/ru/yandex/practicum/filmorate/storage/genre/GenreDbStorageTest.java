package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    void shouldFindAllGenres() {
        List<Genre> genres = genreStorage.findAll();

        assertThat(genres)
                .hasSize(6);
    }

    @Test
    void shouldFindGenreById() {
        Optional<Genre> genre =
                genreStorage.findById(1);

        assertThat(genre)
                .isPresent()
                .hasValueSatisfying(result ->
                        assertThat(result)
                                .hasFieldOrPropertyWithValue("id", 1)
                                .hasFieldOrPropertyWithValue(
                                        "name",
                                        "Комедия"
                                ));
    }

    @Test
    void shouldReturnEmptyWhenGenreNotFound() {
        Optional<Genre> genre =
                genreStorage.findById(999);

        assertThat(genre)
                .isEmpty();
    }
}