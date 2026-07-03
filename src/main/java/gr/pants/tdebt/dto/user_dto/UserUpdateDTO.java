package gr.pants.tdebt.dto.user_dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserUpdateDTO(

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])^.{8,}$")
        String password,

        @NotNull
        String confirmPassword      // CREATE CONFIRM_PASSWORD_VALIDATOR FOR API
) {
}
