package org.shippin.controller.utils;

import javafx.scene.Node;
import org.shippin.domain.enums.Role;
import org.shippin.session.Session;

public class AuthUtils {

    public static void guard(Node node, Role required) {
        boolean allowed = Session.hasAccess(required);
        node.setDisable(!allowed);
        node.setOpacity(allowed ? 1.0 : 0.4);
    }
}
