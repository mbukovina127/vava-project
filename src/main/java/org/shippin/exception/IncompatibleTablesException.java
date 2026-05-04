package org.shippin.exception;

public class IncompatibleTablesException extends Exception {
    public IncompatibleTablesException() {
        super("Price list and region table are not compatible: region codes do not match.");
    }
}