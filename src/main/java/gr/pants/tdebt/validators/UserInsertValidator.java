package gr.pants.tdebt.validators;

import gr.pants.tdebt.dto.user_dto.UserInsertDTO;
import gr.pants.tdebt.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserInsertValidator implements Validator {

    private final IUserService userService;

    @Override
    public boolean supports(Class<?> clazz) {

        return UserInsertDTO.class == clazz;
    }

    @Override
    public void validate(Object target, Errors errors) {

        UserInsertDTO userInsertDTO = (UserInsertDTO) target;

        if (userService.isUserExists(userInsertDTO.email())) {
            log.warn("Save failed. User with email={} already exists", userInsertDTO.email());
            errors.rejectValue(
                    "email",
                    "email.user.exists",
                    "Email already exists"
            );
        }

        if (!userInsertDTO.password().equals(userInsertDTO.confirmPassword())) {
            log.warn("Save failed. Passwords do not match.");
            errors.rejectValue(
                    "confirmPassword",
                    "password.mismatch",
                    "Passwords do not match");
        }
    }
}
