package gr.pants.tdebt.specification;

import gr.pants.tdebt.core.enums.DebtType;
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

    private static Specification<Transaction> belongsToUser(UUID userUuid) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("debt").get("user").get("uuid"), userUuid));
    }

    private static Specification<Transaction> hasDebtType(DebtType debtType) {
        return (root, query, criteriaBuilder) ->
                debtType == null ? criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get("debt").get("type"), debtType);
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

    private static Specification<Transaction> isLatestInChain() {
        return (root, query, criteriaBuilder) -> {
            // Subquery: does ANY transaction reference `root` as its correctedTransaction?
            assert query != null;
            jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
            var correctionRoot = subquery.from(Transaction.class);
            subquery.select(correctionRoot.get("id"));
            subquery.where(criteriaBuilder.equal(correctionRoot.get("correctedTransaction"), root));

            return criteriaBuilder.not(criteriaBuilder.exists(subquery));
        };
    }

    public static Specification<Transaction> build(TransactionFilters filters, UUID debtUuid) {
        return Specification
                .where(belongsToDebt(debtUuid))
                .and(hasAction(filters.action()))
                .and(dateFrom(filters.fromDate()))
                .and(dateTo(filters.toDate()))
                .and(isLatestInChain());
    }

    public static Specification<Transaction> buildForUser(TransactionFilters filters, UUID userUuid) {
        return Specification
                .where(belongsToUser(userUuid))
                .and(hasDebtType(filters.debtType()))
                .and(hasAction(filters.action()))
                .and(dateFrom(filters.fromDate()))
                .and(dateTo(filters.toDate()))
                .and(isLatestInChain());
    }
}
