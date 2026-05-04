package org.shippin.domain.enums;

import java.util.List;

public enum State {
    NOT_READY, READY_FOR_DELIVERY, CANCELED, BEING_DELIVERED, DELIVERED, FAILED;

    public List<State> allowedTransitions() {
        return switch (this) {
            case NOT_READY          -> List.of(READY_FOR_DELIVERY, CANCELED);
            case READY_FOR_DELIVERY -> List.of(BEING_DELIVERED, CANCELED);
            case BEING_DELIVERED    -> List.of(DELIVERED, FAILED);
            case DELIVERED, CANCELED, FAILED -> List.of();
        };
    }
}
