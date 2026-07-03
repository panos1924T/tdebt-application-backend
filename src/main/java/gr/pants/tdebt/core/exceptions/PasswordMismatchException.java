package gr.pants.tdebt.core.exceptions;

public class PasswordMismatchException extends AppGenericException {
    private static final String  DEFAULT_CODE = "PasswordMismatch";

    public PasswordMismatchException(String errorCode, String message) {
        super(errorCode + DEFAULT_CODE, message);
    }
}
