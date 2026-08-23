package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        String sql = """
                INSERT INTO films
                    (name, description, release_date, duration, mpa_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());

            if (film.getMpa() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }

            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());

        saveGenres(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = """
                UPDATE films
                SET name = ?,
                    description = ?,
                    release_date = ?,
                    duration = ?,
                    mpa_id = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId()
        );

        jdbcTemplate.update(
                "DELETE FROM film_genres WHERE film_id = ?",
                film.getId()
        );

        saveGenres(film);

        return film;
    }

    @Override
    public List<Film> findAll() {
        String sql = """
                SELECT
                    f.id,
                    f.name,
                    f.description,
                    f.release_date,
                    f.duration,
                    m.id AS mpa_id,
                    m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                ORDER BY f.id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = mapRowToFilm(rs);

            loadGenres(film);
            loadLikes(film);

            return film;
        });
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = """
                SELECT
                    f.id,
                    f.name,
                    f.description,
                    f.release_date,
                    f.duration,
                    m.id AS mpa_id,
                    m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                WHERE f.id = ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Film film = mapRowToFilm(rs);

                    loadGenres(film);
                    loadLikes(film);

                    return film;
                },
                id
        ).stream().findFirst();
    }

    private Film mapRowToFilm(java.sql.ResultSet rs)
            throws java.sql.SQLException {

        Film film = new Film();

        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(
                rs.getDate("release_date").toLocalDate()
        );
        film.setDuration(rs.getInt("duration"));

        int mpaId = rs.getInt("mpa_id");

        if (!rs.wasNull()) {
            Mpa mpa = new Mpa();
            mpa.setId(mpaId);
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);
        }

        return film;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO film_genres (film_id, genre_id)
                VALUES (?, ?)
                """;

        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(
                    sql,
                    film.getId(),
                    genre.getId()
            );
        }
    }

    private void loadGenres(Film film) {
        String sql = """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;

        Set<Genre> genres = new HashSet<>();

        jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                },
                film.getId()
        ).forEach(genres::add);

        film.setGenres(genres);
    }

    private void loadLikes(Film film) {
        String sql = """
                SELECT user_id
                FROM likes
                WHERE film_id = ?
                """;

        Set<Long> likes = new HashSet<>();

        jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    likes.add(rs.getLong("user_id"));
                    return null;
                },
                film.getId()
        );

        film.setLikes(likes);
    }

    public void addLike(Long filmId, Long userId) {
        String sql = """
                INSERT INTO likes (film_id, user_id)
                VALUES (?, ?)
                """;

        jdbcTemplate.update(sql, filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        String sql = """
                DELETE FROM likes
                WHERE film_id = ?
                  AND user_id = ?
                """;

        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = """
                SELECT
                    f.id,
                    f.name,
                    f.description,
                    f.release_date,
                    f.duration,
                    m.id AS mpa_id,
                    m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                LEFT JOIN likes l ON f.id = l.film_id
                GROUP BY
                    f.id,
                    f.name,
                    f.description,
                    f.release_date,
                    f.duration,
                    m.id,
                    m.name
                ORDER BY COUNT(l.user_id) DESC, f.id
                LIMIT ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Film film = mapRowToFilm(rs);

                    loadGenres(film);
                    loadLikes(film);

                    return film;
                },
                count
        );
    }
}