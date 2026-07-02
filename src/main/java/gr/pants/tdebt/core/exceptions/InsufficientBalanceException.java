package gr.pants.tdebt.core.exceptions;

public class InsufficientBalanceException extends AppGenericException {
    private static final String DEFAULT_CODE = "InsufficientBalance";

    public InsufficientBalanceException(String errorCode, String message) {
        super(errorCode + DEFAULT_CODE, message);
    }
}
