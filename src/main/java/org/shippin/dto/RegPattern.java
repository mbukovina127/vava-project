package org.shippin.dto;

import lombok.Getter;

import java.util.regex.Pattern;

@Getter
public enum RegPattern {
    WORD        ("\\p{L}+"),
    ALPHANUMERIC("[\\p{L}\\p{N}]+"),
    NAME        ("\\p{L}+([\\s\\-']\\p{L}+)*"),
    LANGUAGE    ("[\\p{L}\\s]+"),
    EMAIL       ("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"),
    POSITIVE_DOUBLE("\\d{1,10}(\\.\\d{1,2})?"),
    PERCENT        ("\\d(\\.\\d{1,2})?"),
    POSTAL_CODE ("\\d{3}\\s?\\d{2}");


    private final Pattern pattern;

    RegPattern(String regex) {
        this.pattern = Pattern.compile(regex);
    }

}
