package gr.pants.tdebt.core.exceptions;

import lombok.Getter;
import org.springframework.validation.BindingResult;

@Getter
public class ValidationException extends AppGenericException {
    private static final String DEFAULT_CODE = "ValidationError";
    private final BindingResult bindingResult;

    public ValidationException(String errorCode, String message, BindingResult bindingResult) {
        super(errorCode + DEFAULT_CODE, message);
        this.bindingResult = bindingResult;
    }
}
