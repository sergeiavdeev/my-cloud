
import entities.User;
import org.junit.jupiter.api.*;
import repository.DbUserRepository;
import repository.InMemoryUserRepository;
import repository.UserRepository;
import utils.Hash;

public class UserRepositoryTest {

    private static UserRepository repo;
    private static User user;

    @BeforeAll
    static void init() {
        try {
            repo = DbUserRepository.getInstance();
        } catch (Exception e) {
            repo = InMemoryUserRepository.getInstance();
        }
        user = new User("ivan", "ivan", "Иван","ivan@ivan.ru");
        user.setUuid(Hash.uuid());
    }

    @AfterAll
    static void clear() {
        repo.delete(user);
    }
    @Test
    void add() {

        User duplicateUser = repo.add(user);
        Assertions.assertNull(duplicateUser);
    }

    @Test
    void delete() {

        repo.delete(user);
        User findUser = repo.getById(user.getUuid());
        Assertions.assertNull(findUser);
        repo.add(user);
    }

    @Test
    void update() {

        user.setEmail("test@test.ru");
        repo.update(user);
        user = repo.getById(user.getUuid());
        Assertions.assertEquals(user.getEmail(), "test@test.ru");
    }

    @Test
    void getByCredentials() {

        User wrongUser = repo.getByCredentials("ivan", "wrong");
        Assertions.assertNull(wrongUser);
        user = repo.getByCredentials("ivan", "ivan");
        Assertions.assertNotNull(user);
    }
}
