package org.shippin.database;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Getter
public class Config {

    private String dbDomain;
    private int dbPort;
    private String dbUser;
    private String dbUserPassword;
    private String dbName;

    public Config() {
        loadConfig();
    }

    private void loadConfig() {
        Properties props = new Properties();

        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("config/config.properties")) {

            if (input == null) {
                throw new RuntimeException("config/config.properties not found");
            }

            props.load(input);

            this.dbDomain = props.getProperty("db.domain");
            this.dbPort = Integer.parseInt(props.getProperty("db.port"));
            this.dbUser = props.getProperty("db.user");
            this.dbUserPassword = props.getProperty("db.password");
            this.dbName = props.getProperty("db.name");

        } catch (IOException e) {
            throw new RuntimeException("failed to load config", e);
        }
    }
}