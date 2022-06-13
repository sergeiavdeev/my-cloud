import entities.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import services.UserService;
import services.UserServiceImpl;
import utils.Hash;

public class UserServiceTest {

    private static UserService userService;
    private static User user;

    @BeforeAll
    public static void init() {

        userService = UserServiceImpl.getInstance();
        user = new User("ivan", "ivan", "Иван", "ivan@ivan.ru");
        user.setUuid(Hash.uuid());
        userService.register(user);
    }

    @Test
    public void generateToken() {

        String token = userService.generateToken(user);
        Assertions.assertNotNull(token);
        Assertions.assertFalse(token.isEmpty());
    }

    @Test
    public void validateToken() {

        String token = userService.generateToken(user);
        User validUser = userService.validateToken(token, user.getUuid());
        Assertions.assertNotNull(validUser);

        User notValidUser = userService.validateToken(token, Hash.uuid());
        Assertions.assertNull(notValidUser);
    }

    @Test
    public void auth() {

        Assertions.assertNotNull(userService.auth("ivan", "ivan"));
        Assertions.assertNull(userService.auth("vanya", "vanya"));
    }
}
