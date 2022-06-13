package repository;

import entities.User;
import utils.Hash;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class InMemoryUserRepository implements UserRepository{

    private static UserRepository instance;

    private final HashMap<String, User> users;
    private InMemoryUserRepository() {
        users = new HashMap<>();
        add(new User("admin", "admin", "admin", "admin@admin.ru"));
    }

    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new InMemoryUserRepository();
        }
        return instance;
    }
    @Override
    public User add(User user) {

        AtomicReference<User> findUser = new AtomicReference<>();
        users.forEach((key, value) -> {
            if(value.getLogin().equals(user.getLogin())) {
                findUser.set(value);
            }
        });
        if (findUser.get() != null)return null;

        User newUser = new User(user);
        if (newUser.getUuid().isEmpty())
            newUser.setUuid(Hash.uuid());
        users.put(newUser.getUuid(), newUser);
        return newUser;
    }

    @Override
    public void delete(User user) {
        users.remove(user.getUuid());
    }

    @Override
    public User update(User user) {
        User newUser = new User(user);
        users.put(newUser.getUuid(), newUser);
        return users.get(user.getUuid());
    }

    @Override
    public User getById(String id) {
        return users.get(id);
    }

    @Override
    public User getByCredentials(String login, String password) {

        AtomicReference<User> findUser = new AtomicReference<>();
        users.forEach((key, value) -> {
            if(value.getLogin().equals(login) && value.getPassword().equals(password)) {
                findUser.set(value);
            }
        });
        return findUser.get();
    }
}
