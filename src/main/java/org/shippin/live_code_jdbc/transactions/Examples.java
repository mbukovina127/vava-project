package org.shippin.live_code_jdbc.transactions;


import lombok.NoArgsConstructor;
import org.shippin.live_code_jdbc.Main;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.*;

import static org.shippin.live_code_jdbc.Main.logger;

@NoArgsConstructor
public class Examples {
    Connection conn = null;

    public void run() throws URISyntaxException, SQLException {
        URL dbUrl = Main.class.getResource("/users.db");
        if (dbUrl == null) {
            logger.error("users.db not found in resources");
            return;
        }
        String path = Paths.get(dbUrl.toURI()).toString();
        String url = "jdbc:sqlite:" + path;

        transactionExample(url);

        savepointExample(url);


    }
    public void transactionExample(String url) {
        try {
            conn = DriverManager.getConnection(url);
            conn.setAutoCommit(false);
            System.out.println("State of accounts:");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM accounts limit 2");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + "\t" + rs.getDouble("balance"));
            }

            //take from one account
            PreparedStatement debit = conn.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE id = ?");
            debit.setDouble(1, 100);
            debit.setInt(2, 1);
            debit.executeUpdate();

            //add to the other account
            PreparedStatement credit = conn.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE id = ?");
            credit.setDouble(1, 100);
            credit.setInt(2, 2);
            credit.executeUpdate();

            System.out.println("Intermediate state of accounts:");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM accounts limit 2");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + "\t" + rs.getDouble("balance"));
            }

            // exception
            if (false) {
                System.out.println("Throwing an exception...");
                throw new SQLException();
            }
            System.out.println("Transaction completed correctly...");
            // commiting transaction
            conn.commit();
            //TODO add a logger

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                    //TODO logging
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                System.out.println("Final state of accounts:");
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM accounts limit 2");
                while (rs.next()) {
                    System.out.println(rs.getInt("id") + "\t" + rs.getDouble("balance"));
                }
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void savepointExample(String url) throws SQLException {
        conn = DriverManager.getConnection(url);
        conn.setAutoCommit(false);

        String sql = "INSERT INTO users (first_name, last_name, email, age) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        System.out.println("Newest users:");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM users ORDER BY id desc limit 2");
        while (rs.next()) {
            System.out.println("ID:" + rs.getInt("id") + "\t" + rs.getString("first_name") + "\t" + rs.getString("last_name") + "\t" + rs.getString("email") + "\t" + rs.getInt("age"));
        }

        ps.setString(1, "Bob");
        ps.setString(2, "Petrol");
        ps.setString(3, "petrolBob@mail.com");
        ps.setInt(4, 45);
        ps.executeUpdate();

        // Create savepoint
        Savepoint sp = conn.setSavepoint("BeforeSecondInsert");

        sql = "INSERT INTO users (first_name, last_name, email, age) VALUES (?, ?, ?, ?)";
        ps = conn.prepareStatement(sql);

        ps.setString(1, "Katka");
        ps.setString(2, "Rinka");
        ps.setString(3, "Katkarinka@mail.com");
        ps.setInt(4, 34);
        ps.executeUpdate();

        System.out.println("Updated users:");
        rs = stmt.executeQuery("SELECT * FROM users ORDER BY id desc limit 2");
        while (rs.next()) {
            System.out.println("ID:" + rs.getInt("id") + "\t" + rs.getString("first_name") + "\t" + rs.getString("last_name") + "\t" + rs.getString("email") + "\t" + rs.getInt("age"));
        }

        // Simulate error
        if (true) {
            conn.rollback(sp); // Rollback only second insert
            System.out.println("Rolled back to savepoint...");
            rs = stmt.executeQuery("SELECT * FROM users ORDER BY id desc limit 2");
            while (rs.next()) {
                System.out.println("ID:" + rs.getInt("id") + "\t" + rs.getString("first_name") + "\t" + rs.getString("last_name") + "\t" + rs.getString("email") + "\t" + rs.getInt("age"));
            }
        }

        conn.commit(); // First insert still committed
        System.out.println("After commit");
        rs = stmt.executeQuery("SELECT * FROM users ORDER BY id desc limit 2");
        while (rs.next()) {
            System.out.println("ID:" + rs.getInt("id") + "\t" + rs.getString("first_name") + "\t" + rs.getString("last_name") + "\t" + rs.getString("email") + "\t" + rs.getInt("age"));
        }
        conn.close();
    }
}
