package gr.pants.tdebt.dto.debt_dto;

import gr.pants.tdebt.core.enums.DebtStatus;
import gr.pants.tdebt.core.enums.DebtType;

import java.math.BigDecimal;
import java.time.Instant;

public record DebtReadOnlyDTO(

        String uuid,

        String debtorName,

        DebtType debtType,

        BigDecimal balance,

        String description,

        DebtStatus debtStatus
) {
}
