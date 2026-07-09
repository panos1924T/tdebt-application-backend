package gr.pants.tdebt.api;

import gr.pants.tdebt.core.exceptions.ValidationException;
import gr.pants.tdebt.core.filters.TransactionFilters;
import gr.pants.tdebt.dto.transactions_dto.TransactionInsertDTO;
import gr.pants.tdebt.dto.transactions_dto.TransactionReadOnlyDTO;
import gr.pants.tdebt.dto.transactions_dto.TransactionUpdateDTO;
import gr.pants.tdebt.model.User;
import gr.pants.tdebt.service.ITransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TransactionRestController {

    private final ITransactionService transactionService;

    @PostMapping("/debts/{debtUuid}/transactions")
    public ResponseEntity<TransactionReadOnlyDTO> saveTransaction(
            @Valid @RequestBody TransactionInsertDTO insertDTO,
            @PathVariable UUID debtUuid,
            BindingResult bindingResult,
            @AuthenticationPrincipal User principal
            ) {

        //TODO insertValidator
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Transaction", "Invalid Transaction data", bindingResult);
        }

        TransactionReadOnlyDTO transactionReadOnlyDTO = transactionService.saveTransaction(debtUuid, insertDTO, principal.getUuid());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(transactionReadOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(transactionReadOnlyDTO);
    }

    @PutMapping("/debts/{debtUuid}/transactions/{transUuid}")
    public ResponseEntity<TransactionReadOnlyDTO> updateTransaction(
            @PathVariable UUID debtUuid,
            @PathVariable UUID transUuid,
            @Valid @RequestBody TransactionUpdateDTO updateDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal User principal
            ) {

        //TODO create trans update validator
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Transaction", "Invalid Transaction data", bindingResult);
        }

        TransactionReadOnlyDTO transactionReadOnlyDTO = transactionService.updateTransaction(
                debtUuid, transUuid, updateDTO,
                principal.getUuid());
        return ResponseEntity.ok(transactionReadOnlyDTO);
    }

    @GetMapping("/transactions/{transUuid}")
    public ResponseEntity<TransactionReadOnlyDTO> getTransactionByUuid(
            @PathVariable UUID transUuid,
            @AuthenticationPrincipal User principal) {

        return ResponseEntity.ok(transactionService.getTransactionByUuid(transUuid, principal.getUuid()));
    }

    @GetMapping("/debts/{debtUuid}/transactions")
    public ResponseEntity<Page<TransactionReadOnlyDTO>> getPaginatedFilteredDebtTransactions(
            @AuthenticationPrincipal User principal,
            @PathVariable UUID debtUuid,
            @ModelAttribute TransactionFilters filters,
            @PageableDefault(sort = "date", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        Page<TransactionReadOnlyDTO> transactionPage = transactionService.getPaginatedFilteredDebtTransactions(
                debtUuid, filters, pageable, principal.getUuid());
        return ResponseEntity.ok(transactionPage);
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionReadOnlyDTO>> getPaginatedUserTransactions(
            @AuthenticationPrincipal User principal,
            @PageableDefault(sort = "date", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        Page<TransactionReadOnlyDTO> transactionPage = transactionService.getPaginatedTransactions(
                pageable, principal.getUuid());
        return ResponseEntity.ok(transactionPage);
    }
}
