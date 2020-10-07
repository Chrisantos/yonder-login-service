package com.chriseze.login.restartifacts;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenericEditRequest<T> implements Serializable {
    private static final long serialVersionUID = -2535840198448627779L;

    private T data;
    private String uid;
}
