
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class DbUserRepositoryTest {

    //private final UserRepository repo = new DbUserRepository();

    public DbUserRepositoryTest() throws SQLException, IOException, URISyntaxException {
    }

    @Test
    void addUser() throws IOException, URISyntaxException, SQLException {

        /*
        User user = repo.add(new User(
                "admin",
                "admin",
                "Jon",
                "jon@mail.ru"
        ));

        Assertions.assertNull(user);
         */
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        SecretKey key1 = Keys.secretKeyFor(SignatureAlgorithm.HS512);

        String jws1 = Jwts.builder()
                .setSubject("Avdey")
                .signWith(key)
                .compact();
        String jws2 = Jwts.builder()
                .setSubject("Avdey")
                .signWith(key1)
                .compact();
        System.out.printf("%S\n", jws1);
        System.out.printf("%S\n", jws2);

        Assertions.assertTrue(
                Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(jws1).getBody().getSubject().equals("Avdey"));

        Assertions.assertTrue(
                Jwts.parserBuilder()
                        .setSigningKey(key1)
                        .build()
                        .parseClaimsJws(jws2).getBody().getSubject().equals("Avdey"));

        Assertions.assertFalse(
                Jwts.parserBuilder()
                        .setSigningKey(key1)
                        .build()
                        .parseClaimsJws(jws2).getBody().getSubject().equals("Avdey1"));
    }
}
