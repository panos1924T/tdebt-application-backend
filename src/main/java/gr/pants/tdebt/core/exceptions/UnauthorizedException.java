package gr.pants.tdebt.core.exceptions;

public class UnauthorizedException extends AppGenericException {
    private static final String DEFAULT_CODE = "Unauthorized";

    public UnauthorizedException(String errorCode, String message) {
        super(errorCode + DEFAULT_CODE, message);
    }
}
