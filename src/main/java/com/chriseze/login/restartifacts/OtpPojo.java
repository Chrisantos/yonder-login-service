package com.chriseze.login.restartifacts;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpPojo implements Serializable {

    private static final long serialVersionUID = 7434052175243078855L;

    private String otp;
    private String phoneNumber;
}
