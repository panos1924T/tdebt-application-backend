package gr.pants.tdebt.repository;

import gr.pants.tdebt.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByUuidAndDebt_Uuid(UUID uuid, UUID debtUuid);

    Optional<Transaction> findTransactionByUuidAndDebt_User_UuidAndDebt_DeletedFalse(UUID transactionUuid, UUID userUuid);

    boolean existsByDebt_Uuid(UUID debtUuid);

    boolean existsByCorrectedTransaction_Id(Long originalTransactionId);
}
