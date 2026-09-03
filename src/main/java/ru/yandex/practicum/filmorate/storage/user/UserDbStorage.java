package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Component("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        String sql = """
                INSERT INTO users (email, login, name, birthday)
                VALUES (?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday())
        );

        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?",
                Long.class,
                user.getEmail()
        );

        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        String sql = """
                UPDATE users
                SET email = ?,
                    login = ?,
                    name = ?,
                    birthday = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );

        return user;
    }

    @Override
    public List<User> findAll() {
        String sql = """
                SELECT id, email, login, name, birthday
                FROM users
                ORDER BY id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();

            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));

            Date birthday = rs.getDate("birthday");
            if (birthday != null) {
                user.setBirthday(birthday.toLocalDate());
            }

            return user;
        });
    }

    @Override
    public Optional<User> findUserById(Long id) {
        String sql = """
                SELECT id, email, login, name, birthday
                FROM users
                WHERE id = ?
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    User user = new User();

                    user.setId(rs.getLong("id"));
                    user.setEmail(rs.getString("email"));
                    user.setLogin(rs.getString("login"));
                    user.setName(rs.getString("name"));

                    if (rs.getDate("birthday") != null) {
                        user.setBirthday(
                                rs.getDate("birthday").toLocalDate()
                        );
                    }

                    return user;
                },
                id
        ).stream().findFirst();
    }

    public void addFriend(Long userId, Long friendId) {
        String sql = """
                INSERT INTO friends (user_id, friend_id)
                VALUES (?, ?)
                """;

        jdbcTemplate.update(sql, userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        String sql = """
                DELETE FROM friends
                WHERE user_id = ?
                  AND friend_id = ?
                """;

        jdbcTemplate.update(sql, userId, friendId);
    }

    public List<User> findFriends(Long userId) {
        String sql = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM users u
                JOIN friends f ON f.friend_id = u.id
                WHERE f.user_id = ?
                ORDER BY u.id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();

            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));

            Date birthday = rs.getDate("birthday");
            if (birthday != null) {
                user.setBirthday(birthday.toLocalDate());
            }

            return user;
        }, userId);
    }

    @Override
    public List<User> findCommonFriends(Long userId, Long otherId) {
        String sql = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM users u
                JOIN friends f1 ON f1.friend_id = u.id
                JOIN friends f2 ON f2.friend_id = u.id
                WHERE f1.user_id = ?
                  AND f2.user_id = ?
                ORDER BY u.id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();

            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));

            if (rs.getDate("birthday") != null) {
                user.setBirthday(rs.getDate("birthday").toLocalDate());
            }

            return user;
        }, userId, otherId);
    }

}