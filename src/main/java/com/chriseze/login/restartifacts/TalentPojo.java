package com.chriseze.login.restartifacts;

import com.chriseze.yonder.utils.beanvalidation.ValidEnumString;
import com.chriseze.yonder.utils.enums.Gender;
import com.chriseze.yonder.utils.enums.Industry;
import com.chriseze.yonder.utils.enums.StatusEnum;
import java.io.Serializable;
import java.util.Set;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;

@Getter
@Setter
@ToString
public class TalentPojo implements Serializable {
    private static final long serialVersionUID = -1652166043062776522L;

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

    @ValidEnumString(enumClass = StatusEnum.class, message = "Invalid status type")
    @NotBlank(message = "status is blank")
    private String status;

    @ValidEnumString(enumClass = Industry.class, message = "Invalid industry type")
    @NotBlank(message = "industry is blank")
    private String industry;

    @NotNull(message = "please provide your skills")
    private Set<String> skills;

    @NotBlank(message = "phone number is blank")
    private String phoneNumber;

    @NotNull(message = "please provide your social media handles")
    private Set<SocialMediaPojo> socialMedia;

    private String bio;

    @NotNull(message = "please provide your desired hourly rate")
    private Integer hourlyRate;

    @NotNull(message = "please provide the necessary documents")
    private Set<DocumentPojo> documents;

    private Set<String> recommendations;

}
