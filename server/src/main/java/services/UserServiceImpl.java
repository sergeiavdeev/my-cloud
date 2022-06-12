package services;

import entities.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import repository.DbUserRepository;

import javax.crypto.SecretKey;
import java.util.HashMap;

public class UserServiceImpl implements UserService{

    private final DbUserRepository repository;
    private final HashMap<String, SecretKey> tokens;

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

    public UserServiceImpl(DbUserRepository repository) {
        this.repository = repository;
        tokens = new HashMap<>();
    }

    @Override
    public User auth(String login, String password) {
        return new User();
    }

    @Override
    public void register(User user) {

    }

    @Override
    public void setActivate(User user, boolean active) {

    }

    @Override
    public User update(User user) {
        return null;
    }

    @Override
    public void delete(User user) {

    }

    @Override
    public String generateToken(User user) {

        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String jws = Jwts.builder()
                .setSubject(user.getUuid())
                .signWith(key)
                .compact();
        tokens.put(jws, key);
        return jws;
    }

    @Override
    public User validateToken(String token, String userId) {

        SecretKey key = tokens.get(token);
        try {
            if (Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token).getBody().getSubject().equals(userId)
            ) return repository.getById(userId);
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }
}
