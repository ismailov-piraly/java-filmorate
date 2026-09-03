package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<Mpa> findAll() {
        String sql = """
                SELECT id, name
                FROM mpa
                ORDER BY id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        });
    }

    public Optional<Mpa> findById(Integer id) {
        String sql = """
                SELECT id, name
                FROM mpa
                WHERE id = ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Mpa mpa = new Mpa();
                    mpa.setId(rs.getInt("id"));
                    mpa.setName(rs.getString("name"));
                    return mpa;
                },
                id
        ).stream().findFirst();
    }
}