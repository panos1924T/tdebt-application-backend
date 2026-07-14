package gr.pants.tdebt.core.filters;

import gr.pants.tdebt.core.enums.DebtType;
import gr.pants.tdebt.core.enums.TransactionAction;

import java.time.LocalDate;

public record TransactionFilters(

        TransactionAction action,

        LocalDate fromDate,

        LocalDate toDate,

        DebtType debtType
) {
}
