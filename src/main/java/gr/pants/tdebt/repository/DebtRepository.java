package gr.pants.tdebt.repository;

import gr.pants.tdebt.model.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DebtRepository
        extends JpaRepository<Debt, Long>, JpaSpecificationExecutor<Debt> {
}
