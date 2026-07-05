package gr.pants.tdebt.repository;

import gr.pants.tdebt.model.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface DebtRepository
        extends JpaRepository<Debt, Long>, JpaSpecificationExecutor<Debt> {

    Optional<Debt> findByUuidAndUser_UuidAndDeletedFalse(UUID debtUuid, UUID userUuid);
}
