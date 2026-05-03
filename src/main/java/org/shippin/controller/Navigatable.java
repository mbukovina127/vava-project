package org.shippin.controller;

// Navigatable — interný interface
// package-private — zvonku nikto nevidí

import java.sql.SQLException;

interface Navigatable {
    void onNavigatedTo(Object data) throws SQLException;
}