package repository;

import entities.User;

public interface UserRepository {

    User add(User user);
    void delete(User user);
    User update(User user);
    User getById(String id);
    User getByCredentials(String login, String password);
}
