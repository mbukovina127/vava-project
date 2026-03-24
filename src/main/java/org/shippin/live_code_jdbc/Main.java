package org.shippin.live_code_jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.nio.file.Paths;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        selectUsers();
        insertUser("Viki", "Nova", 21, "viki@test.com");
        selectUsers();
    }

    public static void selectUsers() {
        try {
            // URL users.db v resources
            URL dbUrl = Main.class.getResource("/users.db");
            if (dbUrl == null) {
                logger.error("users.db not found in resources");
                return;
            }
            String path = Paths.get(dbUrl.toURI()).toString();
            String url = "jdbc:sqlite:" + path;

            try (Connection conn = DriverManager.getConnection(url);
                 Statement stmt = conn.createStatement();

                 ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE first_name LIKE 'V%'  LIMIT 5")) {

                System.out.println("SELECT * FROM users LIMIT 5:");
                while (rs.next()) {
                    System.out.println(
                            rs.getInt("id") + " | " +
                                    rs.getString("first_name") + " " +
                                    rs.getString("last_name") + " | " +
                                    rs.getString("email") + " | " +
                                    rs.getInt("age")
                    );
                }
            }

        } catch (Exception e) {
            logger.error("Error reading users", e);
        }
    }

    public static void insertUser(String name, String surname, int age, String email) {
        try {
            URL dbUrl = Main.class.getResource("/users.db");
            if (dbUrl == null) {
                logger.error("users.db not found in resources");
                return;
            }
            String path = Paths.get(dbUrl.toURI()).toString();
            String url = "jdbc:sqlite:" + path;

            String sql = "INSERT INTO users (first_name, last_name, age, email) VALUES (?, ?, ?, ?)";

            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, name);
                ps.setString(2, surname);
                ps.setInt(3, age);
                ps.setString(4, email);

                int rows = ps.executeUpdate();
                System.out.println("Inserted " + rows + " row(s) with name=" + name + " " + surname +
                        " age=" + age + " email=" + email);

            }

        } catch (Exception e) {
            logger.error("Error inserting user", e);
        }
    }
}