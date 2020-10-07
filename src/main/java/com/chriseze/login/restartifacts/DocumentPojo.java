package com.chriseze.login.restartifacts;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentPojo implements Serializable {
    private static final long serialVersionUID = -8119407760613981893L;

    private String title;
    private String document;

}
