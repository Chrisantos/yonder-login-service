package com.chriseze.login.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseEnum {
    SUCCESS("Request was successful", 0),
    SUCCESSFUL_LOGIN("Successfully logged in", 2),
    ERROR("Error occurred processing your request", -1),
    INVALID_REQUEST("Invalid request", -2),
    NO_USER_FOUND("No such user with the provided email address", -3),
    DUPLICATE_USER("User already exists with the email address", -4),
    INVALID_EMAIL_OR_PASSWORD("Email or password not valid", -5),
    ;

    private String description;
    private int code;
}
