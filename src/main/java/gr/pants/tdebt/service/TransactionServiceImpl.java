package gr.pants.tdebt.service;

import gr.pants.tdebt.core.enums.DebtStatus;
import gr.pants.tdebt.core.enums.TransactionAction;
import gr.pants.tdebt.core.exceptions.EntityNotFoundException;
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
import java.util.Collections;
import java.util.List;
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
        transaction.setResultingAmount(transaction.getAmount());
        transaction.setResultingAction(transaction.getAction());

        BigDecimal amount = transaction.getAmount();
        if (transaction.getAction() == TransactionAction.INCREASE) {
            debt.setBalance(debt.getBalance().add(amount));
        } else {
            if (amount.compareTo(debt.getBalance()) > 0) {
                log.warn("Debt's balance is going to be negative! Balance after transaction={}", debt.getBalance().subtract(amount));
            }
            debt.setBalance(debt.getBalance().subtract(amount));
        }

        transaction.setDebt(debt);
        debtRepository.save(debt);

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Transaction with UUID: {} added to Debt with UUID: {}. New Balance: {}",
                savedTransaction.getUuid(), debtUuid, debt.getBalance());
        return transactionMapper.toReadOnlyDTO(savedTransaction, true);
    }

    @Override
    @Transactional
    public TransactionReadOnlyDTO updateTransaction(UUID debtUuid, UUID transUuid, TransactionUpdateDTO updateDTO, UUID userUuid) {

        Debt debt = debtRepository.findByUuidAndUser_UuidAndDeletedFalse(debtUuid, userUuid)
                .orElseThrow(() -> new EntityNotFoundException("Debt", "Debt with uuid=" + debtUuid + " not found"));

        if (debt.getStatus() == DebtStatus.ARCHIVED) {
            throw new InvalidArgumentException("Debt", "Cannot modify transactions on an archived debt");
        }

        Transaction original = transactionRepository
                .findByUuidAndDebt_Uuid(transUuid, debtUuid)
                .orElseThrow(() -> new EntityNotFoundException("Transaction",
                        "Original transaction with uuid=" + transUuid + " not found on this debt"));

        // Calculating Delta (new contribution - original contribution) that matches every scenario.
        // IMPORTANT: use resultingAmount/resultingAction, not amount/action.
        // original.amount/action is only the delta this row itself represents
        // (for a correction row) — using it here would anchor the calculation to
        // the wrong base value when correcting a correction (2nd+ level chain bug).
        BigDecimal originalContribution = original.getResultingAction() == TransactionAction.INCREASE
                ? original.getResultingAmount() : original.getResultingAmount().negate();
        BigDecimal newContribution = updateDTO.action() == TransactionAction.INCREASE
                ? updateDTO.amount() : updateDTO.amount().negate();
        BigDecimal delta = newContribution.subtract(originalContribution);

        // Update only metadata in the same record that no needs to create new record if amount AND action are the same => no financial change.
        if (delta.compareTo(BigDecimal.ZERO) == 0) {
            original.setDate(updateDTO.date());
            original.setNote(updateDTO.note());
            Transaction saved = transactionRepository.save(original);
            log.info("Metadata-only update on Transaction UUID: {}", saved.getUuid());
            return transactionMapper.toReadOnlyDTO(saved, !transactionRepository.existsByCorrectedTransaction_Id(saved.getId()));
        }

        // Anti-chaining check. editing only the last edited version.
        if (transactionRepository.existsByCorrectedTransaction_Id(original.getId())) {
            throw new InvalidArgumentException("Transaction",
                    "Transaction with uuid=" + original.getUuid() +" has already been corrected. Please correct the latest correction instead.");
        }

        BigDecimal resultingBalance = debt.getBalance().add(delta);

        Transaction correction = new Transaction();
        correction.setDate(updateDTO.date());
        correction.setAmount(delta.abs());
        correction.setAction(delta.signum() > 0 ? TransactionAction.INCREASE : TransactionAction.DECREASE);
        correction.setNote(updateDTO.note());
        correction.setCorrectedTransaction(original);
        correction.setResultingAmount(updateDTO.amount());
        correction.setResultingAction(updateDTO.action());
        correction.setDebt(debt);

        debt.setBalance(resultingBalance);
        debtRepository.save(debt);
        Transaction saved = transactionRepository.save(correction);

        log.info("Correction UUID: {} applied to Original UUID: {}. Delta: {}. New Balance: {}",
                saved.getUuid(), original.getUuid(), delta, debt.getBalance());
        return transactionMapper.toReadOnlyDTO(saved, true);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionReadOnlyDTO getTransactionByUuid(UUID transUuid, UUID userUuid) {

        Transaction transaction = getTransactionAndVerifyOwnership(transUuid, userUuid);

        log.info("Transaction with uuid={}, returned successfully", transUuid);
        boolean isLatest = !transactionRepository.existsByCorrectedTransaction_Id(transaction.getId());
        return transactionMapper.toReadOnlyDTO(transaction, isLatest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionReadOnlyDTO> getPaginatedFilteredDebtTransactions(UUID debtUuid, TransactionFilters filters, Pageable pageable, UUID userUuid) {

        Debt debt = debtRepository.findByUuidAndUser_UuidAndDeletedFalse(debtUuid, userUuid)
                .orElseThrow(() -> new EntityNotFoundException("Debt", "Debt with uuid=" + debtUuid + " not found"));

        var specification = TransactionSpecification.build(filters, debtUuid);

        Page<Transaction> page = transactionRepository.findAll(specification, pageable);
        log.info("Filtered and paginated Transactions were returned successfully with page={} and size={}", pageable.getPageNumber(),
                pageable.getPageSize());

        return transactionRepository.findAll(specification, pageable)
                .map(t -> transactionMapper.toReadOnlyDTO(t, true));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionReadOnlyDTO> getPaginatedTransactions(TransactionFilters filters, Pageable pageable, UUID userUuid) {

        var specification = TransactionSpecification.buildForUser(filters, userUuid);

        Page<Transaction> transactionsPage = transactionRepository.findAll(specification, pageable);

        log.info("Filtered and paginated Transactions were returned successfully with page={} and size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        return transactionRepository.findAll(specification, pageable)
                .map(t -> transactionMapper.toReadOnlyDTO(t, true));
    }

    private Transaction getTransactionAndVerifyOwnership(UUID transactionUuid, UUID userUuid) {
        return transactionRepository.findTransactionByUuidAndDebt_User_UuidAndDebt_DeletedFalse(transactionUuid, userUuid)
                .orElseThrow(() -> new EntityNotFoundException("Transaction", "Transaction with uuid=" + transactionUuid + " not found"));
    }
}
