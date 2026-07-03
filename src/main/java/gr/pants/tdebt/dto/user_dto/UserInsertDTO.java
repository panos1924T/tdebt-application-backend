package gr.pants.tdebt.dto.user_dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UserInsertDTO(

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])^.{8,}$")
        String password,

        @NotNull
        String confirmPassword      //TODO CREATE CONFIRM_PASSWORD_VALIDATOR FOR API
) {
}
