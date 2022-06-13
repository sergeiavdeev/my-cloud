package services;

import entities.User;
import io.netty.handler.codec.http.HttpResponse;

public interface UserService {

    User auth(String login, String password);
    void register(User user);
    User update(User user);
    void delete(User user);
    String generateToken(User user);
    User validateToken(String token, String userId);
    HttpResponse authSuccessResponse(String token, String userId);
    HttpResponse authErrorResponse();
}
