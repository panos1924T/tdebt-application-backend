package gr.pants.tdebt.service;

import gr.pants.tdebt.core.filters.TransactionFilters;
import gr.pants.tdebt.dto.transactions_dto.TransactionInsertDTO;
import gr.pants.tdebt.dto.transactions_dto.TransactionReadOnlyDTO;
import gr.pants.tdebt.dto.transactions_dto.TransactionUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ITransactionService {

    TransactionReadOnlyDTO saveTransaction(
            UUID debtUuid, TransactionInsertDTO insertDTO, UUID userUuid
    );

    TransactionReadOnlyDTO updateTransaction(
            UUID debtUuid, TransactionUpdateDTO updateDTO, UUID userUuid
    );

    TransactionReadOnlyDTO getTransactionByUuid(UUID transUuid, UUID userUuid);

    Page<TransactionReadOnlyDTO> getPaginatedFilteredTransactions(
            UUID debtUuid, TransactionFilters filters, Pageable pageable, UUID userUuid
    );
}
