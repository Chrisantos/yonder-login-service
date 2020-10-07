package com.chriseze.login.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OtpResponseEnum {
    SENT_TO_USER("The OTP successfully sent to user."),
    VALID_OTP("The specified otp and msisdn combinations are valid."),
    EXPIRED_OTP("This otp has expired. Please request for a new one."),
    NO_OTP_RECORD("There is no record with the otp, msisdn combination."),
    ERROR_VERIFYING("An error occurred while verifying your otp. Please try again later."),
    ERROR_GENERATING("An error occurred while generating OTP. Please try again later.");

    private String message;
}
