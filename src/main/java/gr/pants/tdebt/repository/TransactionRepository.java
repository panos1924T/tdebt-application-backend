package gr.pants.tdebt.repository;

import gr.pants.tdebt.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    boolean existsByDebt_Uuid(UUID debtUuid);
}
