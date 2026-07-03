package gr.pants.tdebt.dto.debt_dto;

import jakarta.validation.constraints.NotBlank;

public record DebtUpdateDTO(

        @NotBlank
        String debtorName,

        String description
) {
}
