package gr.pants.tdebt.service;

import gr.pants.tdebt.core.enums.DebtStatus;
import gr.pants.tdebt.core.exceptions.DebtHasTransactionsException;
import gr.pants.tdebt.core.exceptions.EntityNotFoundException;
import gr.pants.tdebt.core.filters.DebtFilters;
import gr.pants.tdebt.dto.debt_dto.DebtInsertDTO;
import gr.pants.tdebt.dto.debt_dto.DebtReadOnlyDTO;
import gr.pants.tdebt.dto.debt_dto.DebtUpdateDTO;
import gr.pants.tdebt.mapper.DebtMapper;
import gr.pants.tdebt.model.Debt;
import gr.pants.tdebt.model.User;
import gr.pants.tdebt.repository.DebtRepository;
import gr.pants.tdebt.repository.TransactionRepository;
import gr.pants.tdebt.repository.UserRepository;
import gr.pants.tdebt.specification.DebtSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DebtServiceImpl implements IDebtService {

    private final DebtRepository debtRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final DebtMapper debtMapper;

    @Override
    @Transactional
    public DebtReadOnlyDTO saveDebt(DebtInsertDTO insertDTO, UUID userUuid) {

        User user = userRepository.findUserByUuidAndDeletedFalse(userUuid)
                .orElseThrow(() -> new EntityNotFoundException("User", "User with uuid=" + userUuid + " not found"));

        Debt debt = debtMapper.toEntity(insertDTO);

        debt.setUser(user);
        debt.setStatus(DebtStatus.OPEN);
        debt.setBalance(BigDecimal.ZERO);

        Debt savedDebt = debtRepository.save(debt);
        log.info("Created new Debt with UUID: {} for User: {}", savedDebt.getUuid(), userUuid);

        return debtMapper.toReadOnlyDTO(savedDebt);
    }

    @Override
    @Transactional
    public DebtReadOnlyDTO updateDebt(DebtUpdateDTO updateDTO, UUID debtUuid, UUID userUuid) {

        Debt debt = getDebtAndVerifyOwnership(debtUuid, userUuid);

        debt.setDebtorName(updateDTO.debtorName());
        debt.setDescription(updateDTO.description());

        Debt updatedDebt = debtRepository.save(debt);
        log.info("Updated Debt UUID: {} by User: {}", debtUuid, userUuid);

        return debtMapper.toReadOnlyDTO(updatedDebt);
    }

    @Override
    @Transactional
    public void deleteDebt(UUID debtUuid, UUID userUuid) {

        Debt debt = getDebtAndVerifyOwnership(debtUuid, userUuid);

        if (transactionRepository.existsByDebt_Uuid(debtUuid)) {
            throw new DebtHasTransactionsException("Debt", "Cannot delete Debt with existing transactions");
        }

        debt.softDelete(Instant.now());

        Debt deletedDebt = debtRepository.save(debt);
        log.info("Deleted Debt UUID: {} by User: {}", debtUuid, userUuid);
    }

    @Override
    @Transactional
    public DebtReadOnlyDTO toggleStatus(UUID debtUuid, UUID userUuid) {

        Debt debt = getDebtAndVerifyOwnership(debtUuid, userUuid);

        if (debt.getStatus() == DebtStatus.OPEN) {
            debt.setStatus(DebtStatus.ARCHIVED);
        } else {
            debt.setStatus(DebtStatus.OPEN);
        }

        Debt savedDebt = debtRepository.save(debt);
        log.info("Toggled status to {} for Debt UUID: {}", savedDebt.getStatus(), debtUuid);

        return debtMapper.toReadOnlyDTO(savedDebt);
    }

    @Override
    @Transactional(readOnly = true)
    public DebtReadOnlyDTO getDebtByUuid(UUID debtUuid, UUID userUuid) {

        Debt debt = getDebtAndVerifyOwnership(debtUuid, userUuid);

        return debtMapper.toReadOnlyDTO(debt);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DebtReadOnlyDTO> getFilteredPaginatedDebts(UUID userUuid, DebtFilters filters, Pageable pageable) {

        var specification = DebtSpecification.build(filters, userUuid);

        log.info("Filtered and paginated Debts were returned successfully with page={} and size={}", pageable.getPageNumber(),
                pageable.getPageSize());
        return debtRepository.findAll(specification, pageable)
                .map(debtMapper::toReadOnlyDTO);
    }


    private Debt getDebtAndVerifyOwnership(UUID debtUuid, UUID userUuid) {
        return debtRepository.findByUuidAndUser_UuidAndDeletedFalse(debtUuid, userUuid)
                .orElseThrow(() -> new EntityNotFoundException("Debt", "Debt with uuid=" + debtUuid + " not found"));
    }
}
