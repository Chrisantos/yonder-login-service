package com.chriseze.login.restartifacts;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
public class SocialMediaPojo implements Serializable {
    private static final long serialVersionUID = 382928888021440694L;

    private String name;
    private String handle;

}
