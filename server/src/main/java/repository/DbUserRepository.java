package repository;

import entities.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utils.Config;
import utils.Hash;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.Properties;

public class DbUserRepository implements UserRepository {

    private static UserRepository instance;
    private static Connection connection;
    private PreparedStatement preparedStatement;
    private final Logger logger = LogManager.getLogger(DbUserRepository.class);

    public static UserRepository getInstance() throws SQLException, IOException, URISyntaxException {
        if (instance == null) {
            instance = new DbUserRepository();
        }
        return instance;
    }
    private DbUserRepository() throws SQLException, IOException, URISyntaxException {

        Properties props = new Properties();

        props.setProperty("user", Config.get("db.username"));
        props.setProperty("password", Config.get("db.password"));
        props.setProperty("currentSchema", Config.get("db.schema")); //not work!
        props.setProperty("options", "-c search_path=" + Config.get("db.schema")); //not work!!
        connection = DriverManager.getConnection(Config.get("db.url"), props);
        connection.setSchema(Config.get("db.schema")); //that worked!
        logger.info("Database connect success!");

    }

    @Override
    public User add(User user) {

        try {
            preparedStatement = connection.prepareStatement(SQL_USER_ADD, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, Hash.sha256(user.getLogin(), user.getPassword()));
            logger.trace("Prepare query {}", SQL_USER_ADD);
            preparedStatement.execute();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs.next()) {
                user.setUuid(rs.getString(1));
                return new User(user);
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }

    @Override
    public void delete(User user) {

        try {
            preparedStatement = connection.prepareStatement(SQL_USER_DELETE);
            preparedStatement.setString(1, user.getUuid());
            logger.trace("Prepare query {}", SQL_USER_DELETE);
            preparedStatement.execute();
        } catch (SQLException e) {
            logger.error(e);
        }

    }

    @Override
    public User update(User user) {
        try {
            preparedStatement = connection.prepareStatement(SQL_USER_UPDATE);
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, Hash.sha256(user.getLogin(), user.getPassword()));
            preparedStatement.setBoolean(4, user.isActive());
            preparedStatement.setString(5, user.getUuid());
            logger.trace("Prepare query {}", SQL_USER_UPDATE);
            preparedStatement.execute();
            return new User(user);
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }

    @Override
    public User getById(String id) {

        try {
            preparedStatement = connection.prepareStatement(SQL_USER_SELECT_BY_ID);
            preparedStatement.setString(1, id);

            logger.trace("Prepare query {}", SQL_USER_SELECT_BY_ID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new User(
                        resultSet.getString(1).trim(),
                        resultSet.getString(2).trim(),
                        resultSet.getString(3).trim(),
                        resultSet.getBoolean(5));
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }

    @Override
    public User getByCredentials(String login, String password) {

        try {
            preparedStatement = connection.prepareStatement(SQL_USER_SELECT_BY_HASH);
            preparedStatement.setString(1, Hash.sha256(login, password));
            logger.trace("Prepare query {}", SQL_USER_SELECT_BY_HASH);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new User(
                        resultSet.getString(1).trim(),
                        resultSet.getString(2).trim(),
                        resultSet.getString(3).trim(),
                        resultSet.getBoolean(5));
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }

    private static final String SQL_USER_ADD = "INSERT INTO account " +
            "(name, email, hash)\n" +
            "VALUES(?, ?, ?);";

    private static final String SQL_USER_DELETE = "DELETE FROM account " +
            "where id = uuid(?);";

    private static final String SQL_USER_SELECT_BY_HASH = "SELECT * FROM account " +
            "WHERE hash = ?;";

    private static final String SQL_USER_SELECT_BY_ID = "SELECT * FROM account " +
            "WHERE id = uuid(?);";

    private static final String SQL_USER_UPDATE = "UPDATE account " +
            "SET (name, email, hash, active) = (?, ?, ?, ?) " +
            "WHERE id=uuid(?);";
}
