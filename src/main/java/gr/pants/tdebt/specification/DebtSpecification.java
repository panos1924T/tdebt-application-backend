package gr.pants.tdebt.specification;

import gr.pants.tdebt.core.enums.DebtStatus;
import gr.pants.tdebt.core.enums.DebtType;
import gr.pants.tdebt.core.filters.DebtFilters;
import gr.pants.tdebt.model.Debt;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class DebtSpecification {

    private static Specification<Debt> belongsToUser(UUID userUuid) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("uuid"), userUuid));
    }

    private static Specification<Debt> isNotDeleted() {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get("deleted")));
    }

    private static Specification<Debt> hasStatus(DebtStatus status) {
        return ((root, query, criteriaBuilder) ->
                status == null ? criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get("status"), status));
    }

    private static Specification<Debt> hasType(DebtType type) {
        return ((root, query, criteriaBuilder) ->
                type == null ? criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get("type"), type));
    }

    private static Specification<Debt> hasNameLike(String debtorName) {
        return (root, query, cb) -> {
            if (debtorName == null || debtorName.trim().isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("debtorName")), "%" + debtorName.trim().toLowerCase() + "%");
        };
    }

    public static Specification<Debt> build(DebtFilters filters, UUID userUuid) {
        return Specification
                .where(belongsToUser(userUuid))
                .and(isNotDeleted())
                .and(hasStatus(filters.status()))
                .and(hasType(filters.type()))
                .and(hasNameLike(filters.debtorName()));
    }
}
