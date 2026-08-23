package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    void shouldFindAllMpa() {
        List<Mpa> ratings = mpaStorage.findAll();

        assertThat(ratings)
                .hasSize(5)
                .extracting(Mpa::getId)
                .containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void shouldFindMpaById() {
        Optional<Mpa> mpa =
                mpaStorage.findById(1);

        assertThat(mpa)
                .isPresent()
                .hasValueSatisfying(result ->
                        assertThat(result)
                                .hasFieldOrPropertyWithValue("id", 1)
                                .hasFieldOrPropertyWithValue(
                                        "name",
                                        "G"
                                ));
    }

    @Test
    void shouldReturnEmptyWhenMpaNotFound() {
        Optional<Mpa> mpa =
                mpaStorage.findById(999);

        assertThat(mpa)
                .isEmpty();
    }
}