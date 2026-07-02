package gr.pants.tdebt.core.exceptions;

public class EntityNotFoundException extends AppGenericException {
    private static final String DEFAULT_CODE = "NotFound";

    public EntityNotFoundException(String errorCode, String message) {
        super(errorCode + DEFAULT_CODE, message);
    }
}
