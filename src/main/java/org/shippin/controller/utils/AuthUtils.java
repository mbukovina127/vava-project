package org.shippin.controller.utils;

import javafx.scene.Node;
import org.shippin.domain.enums.Role;
import org.shippin.services.UserService;

public class AuthUtils {

    public static void guard(Node node, Role required) {
        boolean allowed = UserService.hasAccess(required);
        node.setDisable(!allowed);
        node.setOpacity(allowed ? 1.0 : 0.4);
    }
}
