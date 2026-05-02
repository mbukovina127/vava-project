package org.shippin.controller;

// Navigatable — interný interface
// package-private — zvonku nikto nevidí

interface Navigatable {
    void onNavigatedTo(Object data);
}