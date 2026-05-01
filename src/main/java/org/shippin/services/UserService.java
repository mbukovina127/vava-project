package org.shippin.services;

import org.shippin.controller.utils.PasswordUtils;
import org.shippin.database.dao.UserDAO;
import org.shippin.domain.User;
import org.shippin.domain.enums.Role;

import java.sql.SQLException;
import java.util.List;

public class UserService {

    private static User currentUser;

    public static void login(User user) { currentUser = user; }
    public static void logout()         { currentUser = null; }
    public static User getUser()        { return currentUser; }
    public static Role getRole()        { return currentUser != null ? currentUser.getRole() : null; }

    public static boolean hasAccess(Role required) {
        return currentUser != null && currentUser.getRole().ordinal() >= required.ordinal();
    }

    public static User authenticate(String email, String passwordHash) throws SQLException {
        return UserDAO.getInstance().authenticate(email, passwordHash);
    }

    public static User findByEmail(String email) throws SQLException {
        return UserDAO.getInstance().findByEmail(email);
    }

    public static void register(User user) throws SQLException {
        UserDAO.getInstance().insert(user);
    }

    public static List<User> getAllUsers() throws SQLException {
        return UserDAO.getInstance().getAllUsers();
    }

    public static boolean deleteUser(int userId) throws SQLException {
        return UserDAO.getInstance().deleteUser(userId);
    }

    public static void updateRole(int userId, Role role) throws SQLException {
        UserDAO.getInstance().updateRole(userId, role);
    }

}
