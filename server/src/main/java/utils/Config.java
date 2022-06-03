package utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {

    private static Properties props;

    private Config(){}

    public static String get(String name) throws URISyntaxException, IOException {

        if (props == null) {
            props = new Properties();
            props.load(Files.newInputStream(Paths.get(
                    Config.class.getClassLoader().getResource("application.properties").toURI()
            )));
        }
        return props.getProperty(name);
    }
}
