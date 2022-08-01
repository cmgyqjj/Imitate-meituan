package com.hmdp.Exception;

/**
 * @author:{QJJ}
 * @date:{2022}
 * @description:
 **/
public class BadRequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BadRequestException() {
    }

    public BadRequestException(String message) {
        super(message);
    }
}
