package gr.pants.tdebt.core.exceptions;

public class NegativeAmountException extends AppGenericException {

    private static final String DEFAULT_CODE = "NegativeAmount";

    public NegativeAmountException(String errorCode, String message) {
        super(errorCode + DEFAULT_CODE, message);
    }
}
