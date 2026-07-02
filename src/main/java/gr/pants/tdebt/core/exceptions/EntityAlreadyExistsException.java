package gr.pants.tdebt.core.exceptions;

public class EntityAlreadyExistsException extends AppGenericException {
    private static final String DEFAULT_CODE = "AlreadyExists";

    public EntityAlreadyExistsException(String errorCode, String message) {
        super(errorCode + DEFAULT_CODE, message);
    }
}
