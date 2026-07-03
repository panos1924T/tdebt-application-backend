package gr.pants.tdebt.dto.transactions_dto;

import gr.pants.tdebt.core.enums.TransactionAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionInsertDTO(

        @NotNull
        LocalDate date,

        @NotNull
        @Positive
        BigDecimal amount,

        @NotNull
        TransactionAction action,

        String note
) {
}
