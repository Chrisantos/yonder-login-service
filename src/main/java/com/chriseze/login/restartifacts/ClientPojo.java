package com.chriseze.login.restartifacts;

import com.chriseze.yonder.utils.beanvalidation.ValidEnumString;
import com.chriseze.yonder.utils.enums.Gender;
import java.io.Serializable;
import java.util.Set;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;

@Getter
@Setter
@ToString
public class ClientPojo implements Serializable {
    private static final long serialVersionUID = -742499942326130749L;

    @NotBlank(message = "email is blank")
    private String email;

    @NotBlank(message = "password is blank")
    @Size(min = 8, message = "password cannot be less than 8 characters")
    private String password;

    @NotBlank(message = "name is blank")
    private String name;

    @NotBlank(message = "address is blank")
    private String address;

    @ValidEnumString(enumClass = Gender.class, message = "Invalid gender type")
    @NotBlank(message = "status is blank")
    private String gender;

    @NotBlank(message = "phone number is blank")
    private String phoneNumber;

    private Set<String> talents;
}
