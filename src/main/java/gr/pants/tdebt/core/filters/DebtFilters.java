package gr.pants.tdebt.core.filters;

import gr.pants.tdebt.core.enums.DebtStatus;
import gr.pants.tdebt.core.enums.DebtType;

public record DebtFilters(

        DebtStatus status,

        DebtType type,

        String debtorName
) {
}
