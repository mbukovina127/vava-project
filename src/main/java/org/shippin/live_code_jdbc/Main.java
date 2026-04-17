package org.shippin.live_code_jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.nio.file.Paths;
import java.net.URL;
import java.net.URISyntaxException;
import java.sql.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.shippin.live_code_jdbc.transactions.Examples;

public class Main {

    public static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        selectUsers();
        insertUser("Viki", "Nova", 21, "viki@test.com");
        selectUsers();
		//tryCatchExample();
        //tryCatchExampleWithError();
        //batchOperation();
        //updateUser("first_name","Maximilian",8);

        deleteUser(8);
        selectUsers();

//        try {
//            new Examples().run();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

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

                 ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE first_name LIKE 'M%'  LIMIT 2,5")) {

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

            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO users " +
                                 "(first_name, last_name, age, email)" +
                                 " VALUES (?, ?, ?, ?)"
                 )) {

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

     public static void updateUser(String column, String newValue, int id) {
    	try
    	{
            URL dbUrl = Main.class.getResource("/users.db");
            if (dbUrl == null) {
                logger.error("users.db not found in resources");
                return;
            }

            String path = Paths.get(dbUrl.toURI()).toString();
            String url = "jdbc:sqlite:" + path;

	    	String sql = "UPDATE users SET " + column + " = ? WHERE id = ?";

	    	try (Connection conn = DriverManager.getConnection(url);
	    	     PreparedStatement ps = conn.prepareStatement(sql)) {

	    	        ps.setString(1, newValue);
	    	        ps.setInt(2, id);

	    	        int rows = ps.executeUpdate();
	    	        System.out.println("Updated " + rows + " row(s)");
	    	    }

	    	} catch (Exception e) {
	            logger.error("Error updating users", e);
	        }
    	}

    public static void deleteUser(int id) {
    	try
    	{
    		URL dbUrl = Main.class.getResource("/users.db");
            if (dbUrl == null) {
                logger.error("users.db not found in resources");
                return;
            }

            String path = Paths.get(dbUrl.toURI()).toString();
            String url = "jdbc:sqlite:" + path;

	    	String sql = "DELETE FROM users WHERE id = ?";

	    	try (Connection conn = DriverManager.getConnection(url);
	    	     PreparedStatement ps = conn.prepareStatement(sql)) {

	    	        ps.setInt(1, id);

	    	        int rows = ps.executeUpdate();
	    	        System.out.println("Deleted " + rows + " row(s)");
	    	    }

	    	} catch (Exception e) {
	            logger.error("Error deleting users", e);
	        }
    	}

	public static void tryCatchExample() {
        URL dbUrl = Main.class.getResource("/users.db");
        if (dbUrl == null) {
            logger.error("users.db not found in resources");
            return;
        }

        try {
            String path = Paths.get(dbUrl.toURI()).toString();
            String url = "jdbc:sqlite:" + path;

            String sql = "SELECT id, first_name, last_name, email, age FROM users LIMIT 5";

            System.out.println("tryCatchExample - first 5 users:");
            try (Connection conn = DriverManager.getConnection(url);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    System.out.println(
                            rs.getInt("id") + " | " +
                                    rs.getString("first_name") + " " +
                                    rs.getString("last_name") + " | " +
                                    rs.getString("email") + " | " +
                                    rs.getInt("age")
                    );
                }

            } catch (SQLException e) {
                System.out.println("Database error while reading users.");
                System.out.println("Message: " + e.getMessage());
                System.out.println("SQL state: " + e.getSQLState());
                System.out.println("Error code: " + e.getErrorCode());

                logger.error("SQL error in tryCatchExample", e);
            }

        } catch (URISyntaxException e) {
            logger.error("Invalid path to users.db", e);
        }
    }

    public static void tryCatchExampleWithError() {
        URL dbUrl = Main.class.getResource("/users.db");
        if (dbUrl == null) {
            logger.error("users.db not found in resources");
            return;
        }

        try {
            String path = Paths.get(dbUrl.toURI()).toString();
            String url = "jdbc:sqlite:" + path;

            // intentionally wrong table name to trigger SQLException
            String sql = "SELECT * FROM userz LIMIT 5";

            System.out.println("tryCatchExampleWithError - first 5 users:");
            try (Connection conn = DriverManager.getConnection(url);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                System.out.println("This line will not be reached if SQL fails.");

            } catch (SQLException e) {
                System.out.println("Database error occurred.");
                System.out.println("Message: " + e.getMessage());
                System.out.println("SQL state: " + e.getSQLState());
                System.out.println("Error code: " + e.getErrorCode());

                logger.error("SQL error in tryCatchExampleWithError", e);
            }

        } catch (URISyntaxException e) {
            logger.error("Invalid path to users.db", e);
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