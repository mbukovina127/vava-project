package org.shippin.live_code_jdbc;

import java.net.URISyntaxException;
import java.sql.*;
import java.nio.file.Paths;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.shippin.live_code_jdbc.transactions.Examples;

public class Main {

    public static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        selectUsers();
        insertUser("Viki", "Nova", 21, "viki@test.com");
        selectUsers();
        batchOperation();

        try {
            new Examples().run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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


    public static void batchOperation() {
        try {
            URL dbUrl = Main.class.getResource("/users.db");
            if (dbUrl == null) {
                logger.error("users.db not found in resources");
                return;
            }
            String path = Paths.get(dbUrl.toURI()).toString();
            String url = "jdbc:sqlite:" + path;

            String sql = "UPDATE users SET age = ?, email = ? WHERE id = ?";

            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                conn.setAutoCommit(false);

                ps.setInt(1, 59);
                ps.setString(2, "katarina.farkas@newdomain.com");
                ps.setInt(3, 1);
                ps.addBatch();

                ps.setInt(1, 50);
                ps.setString(2, "adam.mikula@newdomain.com");
                ps.setInt(3, 2);
                ps.addBatch();

                ps.setInt(1, 63);
                ps.setString(2, "eva.polak@newdomain.com");
                ps.setInt(3, 3);
                ps.addBatch();

                ps.setInt(1, 46);
                ps.setString(2, "filip.mikula@newdomain.com");
                ps.setInt(3, 4);
                ps.addBatch();

                ps.setInt(1, 44);
                ps.setString(2, "tomas.mikula@newdomain.com");
                ps.setInt(3, 5);
                ps.addBatch();

                int[] results = ps.executeBatch();

                conn.commit();

                for (int i = 0; i < results.length; i++) {
                    System.out.println("statement " + (i + 1) + " updated " + results[i] + " rows");
                }
            }

        } catch (Exception e) {
            logger.error("error batch operation", e);
        }
    }
}