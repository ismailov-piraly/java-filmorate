package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @Test
    void shouldFindUserById() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        Optional<User> foundUser = userStorage.findUserById(createdUser.getId());

        assertThat(foundUser)
                .isPresent()
                .hasValueSatisfying(found -> assertThat(found)
                        .hasFieldOrPropertyWithValue("id", createdUser.getId())
                        .hasFieldOrPropertyWithValue("email", "test@example.com")
                        .hasFieldOrPropertyWithValue("login", "testLogin"));
    }


    @Test
    void shouldFindAllUsers() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));

        userStorage.create(user1);
        userStorage.create(user2);

        List<User> users = userStorage.findAll();

        assertThat(users)
                .hasSize(2)
                .extracting(User::getLogin)
                .containsExactly("user1", "user2");
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setEmail("old@example.com");
        user.setLogin("oldLogin");
        user.setName("Old Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        createdUser.setEmail("new@example.com");
        createdUser.setLogin("newLogin");
        createdUser.setName("New Name");

        userStorage.update(createdUser);

        Optional<User> updatedUser =
                userStorage.findUserById(createdUser.getId());

        assertThat(updatedUser)
                .isPresent()
                .hasValueSatisfying(userFromDb -> assertThat(userFromDb)
                        .hasFieldOrPropertyWithValue("email", "new@example.com")
                        .hasFieldOrPropertyWithValue("login", "newLogin")
                        .hasFieldOrPropertyWithValue("name", "New Name"));
    }

    @Test
    void shouldAddAndRemoveFriend() {
        User user1 = createUser("user1@example.com", "user1");
        User user2 = createUser("user2@example.com", "user2");

        userStorage.addFriend(user1.getId(), user2.getId());

        List<User> friends = userStorage.findFriends(user1.getId());

        assertThat(friends)
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("id", user2.getId());

        userStorage.removeFriend(user1.getId(), user2.getId());

        assertThat(userStorage.findFriends(user1.getId()))
                .isEmpty();
    }

    @Test
    void shouldFindCommonFriends() {
        User user1 = createUser("user1@example.com", "user1");
        User user2 = createUser("user2@example.com", "user2");
        User commonFriend = createUser("friend@example.com", "friend");

        userStorage.addFriend(user1.getId(), commonFriend.getId());
        userStorage.addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends =
                userStorage.findCommonFriends(
                        user1.getId(),
                        user2.getId()
                );

        assertThat(commonFriends)
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue(
                        "id",
                        commonFriend.getId()
                );
    }

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        return userStorage.create(user);
    }

    @Test
    void friendshipShouldBeOneSided() {
        User user1 = createUser("user1@example.com", "user1");
        User user2 = createUser("user2@example.com", "user2");

        userStorage.addFriend(user1.getId(), user2.getId());

        assertThat(userStorage.findFriends(user1.getId()))
                .extracting(User::getId)
                .containsExactly(user2.getId());

        assertThat(userStorage.findFriends(user2.getId()))
                .isEmpty();
    }
}