package gr.pants.tdebt.service;

import gr.pants.tdebt.core.filters.DebtFilters;
import gr.pants.tdebt.dto.debt_dto.DebtInsertDTO;
import gr.pants.tdebt.dto.debt_dto.DebtReadOnlyDTO;
import gr.pants.tdebt.dto.debt_dto.DebtUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IDebtService {

    DebtReadOnlyDTO saveDebt(DebtInsertDTO insertDTO, UUID userUuid);

    DebtReadOnlyDTO updateDebt(DebtUpdateDTO updateDTO, UUID debtUuid, UUID userUuid);

    DebtReadOnlyDTO deleteDebt(UUID debtUuid, UUID userUuid);

    DebtReadOnlyDTO toggleStatus(UUID debtUuid, UUID userUuid);

    DebtReadOnlyDTO getDebtByUuid(UUID debtUuid, UUID userUuid);

    Page<DebtReadOnlyDTO> getFilteredPaginatedDebts(UUID userUuid, DebtFilters filters, Pageable pageable);
}
