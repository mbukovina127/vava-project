package org.shippin.services;

import org.shippin.domain.User;
import org.shippin.domain.enums.Role;

public class UserService {

    private static User currentUser;

    public static void login(User user) { currentUser = user; }
    public static void logout()         { currentUser = null; }
    public static User getUser()        { return currentUser; }
    public static Role getRole()        { return currentUser != null ? currentUser.getRole() : null; }

    public static boolean hasAccess(Role required) {
        return currentUser != null && currentUser.getRole().ordinal() >= required.ordinal();
    }
}
