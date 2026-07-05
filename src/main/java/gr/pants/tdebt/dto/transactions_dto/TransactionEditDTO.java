package gr.pants.tdebt.dto.transactions_dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TransactionEditDTO(

        @NotNull
        LocalDate date,

        String note
) {
}
