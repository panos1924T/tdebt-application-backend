package gr.pants.tdebt.dto.debt_dto;

import gr.pants.tdebt.core.enums.DebtType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DebtInsertDTO(

        @NotBlank
        @Size(min = 3, max = 50)
        String debtorName,

        @NotNull
        DebtType debtType,

        String description
) {
}
