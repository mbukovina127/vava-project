package org.shippin.services;

import org.shippin.database.dao.UserDAO;
import org.shippin.domain.User;
import org.shippin.domain.enums.Role;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class UserService {

    private static final Logger log = Logger.getLogger(UserService.class.getName());
    private static User currentUser;

    public static void login(User user) { currentUser = user; log.info("User logged in: " + user.getEmail()); }
    public static void logout()         { log.info("User logged out: " + (currentUser != null ? currentUser.getEmail() : "none")); currentUser = null; }
    public static User getUser()        { return currentUser; }
    public static Role getRole()        { return currentUser != null ? currentUser.getRole() : null; }

    public static boolean hasAccess(Role required) {
        return currentUser != null && currentUser.getRole().ordinal() >= required.ordinal();
    }

    private static String hash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((salt + password).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static User authenticate(String email, String plainPassword) throws SQLException {
        User user = UserDAO.getInstance().authenticate(email, hash(plainPassword, email));
        if (user != null) login(user); else log.warning("Failed login attempt for: " + email);
        return user;
    }

    public static User findByEmail(String email) throws SQLException {
        return UserDAO.getInstance().findByEmail(email);
    }

    public static void register(User user, String plainPassword) throws SQLException {
        user.setPassword(hash(plainPassword, user.getEmail()));
        UserDAO.getInstance().insert(user);
        log.info("Registered new user: " + user.getEmail());
    }
    public static User getUser(int userId) throws SQLException {
        return UserDAO.getInstance().GetUser(userId);
    }
    public static List<User> getAllUsers() throws SQLException {
        return UserDAO.getInstance().getAllUsers();
    }

    public static boolean deleteUser(int userId) throws SQLException {
        boolean deleted = UserDAO.getInstance().deleteUser(userId);
        if (deleted) log.info("Deleted user: " + userId);
        return deleted;
    }

    public static void updateRole(int userId, Role role) throws SQLException {
        UserDAO.getInstance().updateRole(userId, role);
        log.info("Updated role for user " + userId + " to " + role);
    }
}
