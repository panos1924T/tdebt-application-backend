package gr.pants.tdebt.repository;

import gr.pants.tdebt.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByUuidAndDebt_Uuid(UUID uuid, UUID debtUuid);

    Optional<Transaction> findTransactionByUuidAndDebt_User_UuidAndDebt_DeletedFalse(UUID transactionUuid, UUID userUuid);

    boolean existsByDebt_Uuid(UUID debtUuid);

    boolean existsByCorrectedTransaction_Id(Long originalTransactionId);

    @Query("SELECT DISTINCT t.correctedTransaction.id FROM Transaction t WHERE t.correctedTransaction.id IN :ids")
    Set<Long> findCorrectedTransactionIdsIn(@Param("ids") Collection<Long> ids);
}
