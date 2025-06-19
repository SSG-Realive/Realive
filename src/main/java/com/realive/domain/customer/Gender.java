package com.realive.domain.customer;

public enum Gender {
    M("MALE"),  // DB의 'M'을 MALE로 매핑
    F("FEMALE"); // DB의 'F'를 FEMALE로 매핑

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}