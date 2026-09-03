package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;
    @Autowired
    private UserDbStorage userStorage;

    @Test
    void shouldCreateAndFindFilm() {
        Film film = new Film();

        film.setName("Test Film");
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        Film created = filmStorage.create(film);

        Optional<Film> found =
                filmStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(result ->
                        assertThat(result)
                                .hasFieldOrPropertyWithValue(
                                        "id",
                                        created.getId()
                                )
                                .hasFieldOrPropertyWithValue(
                                        "name",
                                        "Test Film"
                                ));
    }

    @Test
    void shouldFindAllFilms() {
        Film film1 = createFilm("Film 1");
        Film film2 = createFilm("Film 2");

        List<Film> films = filmStorage.findAll();

        assertThat(films)
                .hasSize(2)
                .extracting(Film::getName)
                .containsExactly("Film 1", "Film 2");
    }

    @Test
    void shouldUpdateFilm() {
        Film film = createFilm("Old name");

        film.setName("New name");
        film.setDescription("New description");

        filmStorage.update(film);

        Optional<Film> updated =
                filmStorage.findById(film.getId());

        assertThat(updated)
                .isPresent()
                .hasValueSatisfying(result ->
                        assertThat(result)
                                .hasFieldOrPropertyWithValue(
                                        "name",
                                        "New name"
                                )
                                .hasFieldOrPropertyWithValue(
                                        "description",
                                        "New description"
                                ));
    }

    @Test
    void shouldAddAndRemoveLike() {
        User user = new User();
        user.setEmail("like@example.com");
        user.setLogin("likeUser");
        user.setName("Like User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        Film film = createFilm("Liked film");

        filmStorage.addLike(film.getId(), createdUser.getId());

        Optional<Film> liked =
                filmStorage.findById(film.getId());

        assertThat(liked)
                .isPresent()
                .hasValueSatisfying(result ->
                        assertThat(result.getLikes())
                                .contains(createdUser.getId())
                );

        filmStorage.deleteLike(film.getId(), createdUser.getId());

        Optional<Film> unliked =
                filmStorage.findById(film.getId());

        assertThat(unliked)
                .isPresent()
                .hasValueSatisfying(result ->
                        assertThat(result.getLikes())
                                .doesNotContain(createdUser.getId())
                );
    }

    private Film createFilm(String name) {
        Film film = new Film();

        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        return filmStorage.create(film);
    }
}