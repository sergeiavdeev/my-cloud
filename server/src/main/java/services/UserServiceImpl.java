package services;

import entities.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import repository.InMemoryUserRepository;
import repository.UserRepository;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class UserServiceImpl implements UserService{

    public static UserService instance;
    private final UserRepository userRepository;

    private final HashMap<String, SecretKey> tokens;
    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

    private UserServiceImpl() {
        userRepository = InMemoryUserRepository.getInstance();
        tokens = new HashMap<>();
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserServiceImpl();
        }
        return instance;
    }
    @Override
    public User auth(String login, String password) {
        return userRepository.getByCredentials(login, password);
    }

    @Override
    public void register(User user) {
        userRepository.add(user);
    }

    @Override
    public User update(User user) {
        return userRepository.update(user);
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
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
            ) return userRepository.getById(userId);
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    @Override
    public HttpResponse authSuccessResponse(String token, String userId) {

        JSONObject ob = new JSONObject();
        ob.put("token", token);
        ob.put("userId", userId);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.wrappedBuffer(ob.toString().getBytes(StandardCharsets.UTF_8)));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        return response;
    }

    @Override
    public HttpResponse authErrorResponse() {

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, UNAUTHORIZED);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        return response;
    }
}
