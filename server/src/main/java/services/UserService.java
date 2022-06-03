package services;

import entities.User;

public interface UserService {

    User auth(String login, String password);
    void register(User user);
    void setActivate(User user, boolean active);
    User update(User user);
    void delete(User user);

    String generateToken(User user);
    User validateToken(String token, String userId);
}
