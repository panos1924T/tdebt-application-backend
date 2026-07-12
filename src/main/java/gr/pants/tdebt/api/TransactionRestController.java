package gr.pants.tdebt.api;

import gr.pants.tdebt.core.exceptions.ValidationException;
import gr.pants.tdebt.core.filters.TransactionFilters;
import gr.pants.tdebt.dto.transactions_dto.TransactionInsertDTO;
import gr.pants.tdebt.dto.transactions_dto.TransactionReadOnlyDTO;
import gr.pants.tdebt.dto.transactions_dto.TransactionUpdateDTO;
import gr.pants.tdebt.model.User;
import gr.pants.tdebt.service.ITransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionRestController {

    private final ITransactionService transactionService;

    @Operation(summary="Create a Transaction", description="Creates a new Transaction for a specific Debt")
    @ApiResponses({
            @ApiResponse(responseCode="201", description="Created", content=@Content(schema=@Schema(implementation=TransactionReadOnlyDTO.class))),
            @ApiResponse(responseCode="400", description="Validation error"),
            @ApiResponse(responseCode="404", description="Debt not found")
    })
    @PostMapping("/debts/{debtUuid}/transactions")
    public ResponseEntity<TransactionReadOnlyDTO> saveTransaction(
            @Valid @RequestBody TransactionInsertDTO insertDTO,
            BindingResult bindingResult,
            @PathVariable UUID debtUuid,
            @AuthenticationPrincipal User principal
            ) {

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


    @Operation(summary="Update a Transaction", description="Updates an existing Transaction")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="OK", content=@Content(schema=@Schema(implementation=TransactionReadOnlyDTO.class))),
            @ApiResponse(responseCode="400", description="Validation error"),
            @ApiResponse(responseCode="404", description="Not found")
    })
    @PutMapping("/debts/{debtUuid}/transactions/{transUuid}")
    public ResponseEntity<TransactionReadOnlyDTO> updateTransaction(
            @PathVariable UUID debtUuid,
            @PathVariable UUID transUuid,
            @Valid @RequestBody TransactionUpdateDTO updateDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal User principal
            ) {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Transaction", "Invalid Transaction data", bindingResult);
        }

        TransactionReadOnlyDTO transactionReadOnlyDTO = transactionService.updateTransaction(
                debtUuid, transUuid, updateDTO,
                principal.getUuid());
        return ResponseEntity.ok(transactionReadOnlyDTO);
    }


    @Operation(summary="Get a Transaction", description="Returns a Transaction by UUID")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="OK", content=@Content(schema=@Schema(implementation=TransactionReadOnlyDTO.class))),
            @ApiResponse(responseCode="404", description="Not found")
    })
    @GetMapping("/transactions/{transUuid}")
    public ResponseEntity<TransactionReadOnlyDTO> getTransactionByUuid(
            @PathVariable UUID transUuid,
            @AuthenticationPrincipal User principal) {

        return ResponseEntity.ok(transactionService.getTransactionByUuid(transUuid, principal.getUuid()));
    }


    @Operation(summary="List Debt Transactions", description="Returns paginated and filtered Transactions for a specific Debt")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="OK", content=@Content(schema=@Schema(implementation=org.springframework.data.domain.Page.class))),
            @ApiResponse(responseCode="404", description="Debt not found")
    })
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


    @Operation(summary="List User Transactions", description="Returns all paginated Transactions for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="OK", content=@Content(schema=@Schema(implementation=org.springframework.data.domain.Page.class)))
    })
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
