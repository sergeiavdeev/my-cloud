package repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class UserRepository {

    private static Connection connection;
    private PreparedStatement preparedStatement;
    private final static Logger log = LogManager.getLogger();

    public UserRepository() throws SQLException, IOException {

        Properties property = new Properties();
        property.load(Files.newInputStream(Paths.get("server/src/main/resources/application.properties")));

        Properties props = new Properties();
        props.setProperty("user", property.getProperty("db.username"));
        props.setProperty("password", property.getProperty("db.password"));
        props.setProperty("currentSchema", property.getProperty("db.schema"));
        connection = DriverManager.getConnection(property.getProperty("db.url"), props);
        log.info("Database connect success!");
    }
}
