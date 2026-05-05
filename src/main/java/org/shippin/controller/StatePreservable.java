package org.shippin.controller;

public interface StatePreservable {
    Object captureState();
    void restoreState(Object state);
}
