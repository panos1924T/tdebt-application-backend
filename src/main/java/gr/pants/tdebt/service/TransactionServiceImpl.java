package gr.pants.tdebt.service;

import gr.pants.tdebt.core.enums.DebtStatus;
import gr.pants.tdebt.core.enums.TransactionAction;
import gr.pants.tdebt.core.exceptions.EntityNotFoundException;
import gr.pants.tdebt.core.exceptions.InsufficientBalanceException;
import gr.pants.tdebt.core.exceptions.InvalidArgumentException;
import gr.pants.tdebt.core.exceptions.NegativeAmountException;
import gr.pants.tdebt.core.filters.TransactionFilters;
import gr.pants.tdebt.dto.transactions_dto.TransactionInsertDTO;
import gr.pants.tdebt.dto.transactions_dto.TransactionReadOnlyDTO;
import gr.pants.tdebt.dto.transactions_dto.TransactionUpdateDTO;
import gr.pants.tdebt.mapper.TransactionMapper;
import gr.pants.tdebt.model.Debt;
import gr.pants.tdebt.model.Transaction;
import gr.pants.tdebt.repository.DebtRepository;
import gr.pants.tdebt.repository.TransactionRepository;
import gr.pants.tdebt.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements ITransactionService {

    private final TransactionRepository transactionRepository;
    private final DebtRepository debtRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransactionReadOnlyDTO saveTransaction(UUID debtUuid, TransactionInsertDTO insertDTO, UUID userUuid) {

        Debt debt = debtRepository.findByUuidAndUser_UuidAndDeletedFalse(debtUuid, userUuid)
                .orElseThrow(() -> new EntityNotFoundException("Debt", "Debt with uuid=" + debtUuid + " not found"));
        if (debt.getStatus() == DebtStatus.ARCHIVED) {
            throw new InvalidArgumentException("Transaction", "Cannot add transactions to an archived debt");
        }
        if (insertDTO.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegativeAmountException("Transaction", "Cannot process transaction with ZERO or negative amount");
        }

        Transaction transaction = transactionMapper.toEntity(insertDTO);

        BigDecimal amount = transaction.getAmount();
        if (transaction.getAction() == TransactionAction.INCREASE) {
            debt.setBalance(debt.getBalance().add(amount));
        } else {
            if (amount.compareTo(debt.getBalance()) > 0) {
                throw new InsufficientBalanceException("Transaction", "Cannot decrease balance below zero. Current balance is: " + debt.getBalance());
            }
            debt.setBalance(debt.getBalance().subtract(amount));
        }

        transaction.setDebt(debt);
        debtRepository.save(debt);

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Transaction with UUID: {} added to Debt with UUID: {}. New Balance: {}",
                savedTransaction.getUuid(), debtUuid, debt.getBalance());
        return transactionMapper.toReadOnlyDTO(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionReadOnlyDTO updateTransaction(UUID transUuid, TransactionUpdateDTO updateDTO, UUID userUuid) {

        Transaction transaction = getTransactionAndVerifyOwnership(transUuid, userUuid);

        transaction.setDate(updateDTO.date());
        transaction.setNote(updateDTO.note());

        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("Updated metadata for Transaction with UUID: {}", transUuid);

        return transactionMapper.toReadOnlyDTO(updatedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionReadOnlyDTO getTransactionByUuid(UUID transUuid, UUID userUuid) {

        Transaction transaction = getTransactionAndVerifyOwnership(transUuid, userUuid);

        log.info("Transaction with uuid={}, returned successfully", transUuid);
        return transactionMapper.toReadOnlyDTO(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionReadOnlyDTO> getPaginatedFilteredTransactions(UUID debtUuid, TransactionFilters filters, Pageable pageable, UUID userUuid) {

        Debt debt = debtRepository.findByUuidAndUser_UuidAndDeletedFalse(debtUuid, userUuid)
                .orElseThrow(() -> new EntityNotFoundException("Debt", "Debt with uuid=" + debtUuid + " not found"));

        var specification = TransactionSpecification.build(filters, debtUuid);

        log.info("Filtered and paginated Transactions were returned successfully with page={} and size={}", pageable.getPageNumber(),
                pageable.getPageSize());
        return transactionRepository.findAll(specification, pageable)
                .map(transactionMapper::toReadOnlyDTO);
    }

    private Transaction getTransactionAndVerifyOwnership(UUID transactionUuid, UUID userUuid) {
        return transactionRepository.findTransactionByUuidAndDebt_User_UuidAndDebt_DeletedFalse(transactionUuid, userUuid)
                .orElseThrow(() -> new EntityNotFoundException("Transaction", "Transaction with uuid=" + transactionUuid + " not found"));
    }
}
