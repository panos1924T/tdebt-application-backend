package gr.pants.tdebt.dto.transactions_dto;

import gr.pants.tdebt.core.enums.TransactionAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TransactionReadOnlyDTO(

        String uuid,

        LocalDate date,

        BigDecimal amount,

        String action,

        String note,

        Instant createdAt
) {
}
