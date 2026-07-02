package gr.pants.tdebt.core.exceptions;

public class InvalidArgumentException extends AppGenericException {
    private static final String DEFAULT_CODE = "InvalidArgument";

    public InvalidArgumentException(String errorCode, String message) {
        super(errorCode + DEFAULT_CODE, message);
    }
}
