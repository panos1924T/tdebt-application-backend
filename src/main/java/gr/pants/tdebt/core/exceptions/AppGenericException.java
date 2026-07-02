package gr.pants.tdebt.core.exceptions;

import lombok.Getter;

@Getter
public class AppGenericException extends RuntimeException {
    private final String errorCode;

    public AppGenericException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
