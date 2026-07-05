package gr.pants.tdebt.specification;

import gr.pants.tdebt.core.enums.TransactionAction;
import gr.pants.tdebt.core.filters.TransactionFilters;
import gr.pants.tdebt.model.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class TransactionSpecification {

    private static Specification<Transaction> belongsToDebt(UUID debtUuid) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("debt").get("uuid"), debtUuid));
    }

    private static Specification<Transaction> hasAction(TransactionAction action) {
        return (root, query, criteriaBuilder) ->
                action == null ? criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get("action"), action);
    }

    private static Specification<Transaction> dateFrom(LocalDate fromDate) {
        return (root, query, criteriaBuilder) ->
                fromDate == null ? criteriaBuilder.conjunction() :
                        criteriaBuilder.greaterThanOrEqualTo(root.get("date"), fromDate);
    }

    private static Specification<Transaction> dateTo(LocalDate toDate) {
        return (root, query, criteriaBuilder) ->
                toDate == null ? criteriaBuilder.conjunction() :
                        criteriaBuilder.lessThanOrEqualTo(root.get("date"), toDate);
    }

    public static Specification<Transaction> build(TransactionFilters filters, UUID debtUuid) {
        return Specification
                .where(belongsToDebt(debtUuid))
                .and(hasAction(filters.action()))
                .and(dateFrom(filters.fromDate()))
                .and(dateTo(filters.toDate()));
    }
}
