package org.shippin.dto;

import java.util.regex.Pattern;

public enum RegPattern {
    WORD        ("\\p{L}+"),
    ALPHANUMERIC("[\\p{L}\\p{N}]+"),
    NAME        ("\\p{L}+([\\s\\-']\\p{L}+)*"),
    LANGUAGE    ("[\\p{L}\\s]+"),
    EMAIL       ("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private final Pattern pattern;

    RegPattern(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public Pattern getPattern() {
        return pattern;
    }
}
