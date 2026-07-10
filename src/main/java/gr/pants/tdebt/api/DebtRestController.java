package gr.pants.tdebt.api;

import gr.pants.tdebt.core.exceptions.ValidationException;
import gr.pants.tdebt.core.filters.DebtFilters;
import gr.pants.tdebt.dto.debt_dto.DebtInsertDTO;
import gr.pants.tdebt.dto.debt_dto.DebtReadOnlyDTO;
import gr.pants.tdebt.dto.debt_dto.DebtUpdateDTO;
import gr.pants.tdebt.model.User;
import gr.pants.tdebt.service.IDebtService;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/debts")
public class DebtRestController {

    private final IDebtService debtService;

    @PostMapping
    public ResponseEntity<DebtReadOnlyDTO> saveDebt(
            @Valid @RequestBody DebtInsertDTO insertDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal User principal
            ) {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Debt", "Invalid Debt data", bindingResult);
        }

        DebtReadOnlyDTO debtReadOnlyDTO = debtService.saveDebt(insertDTO, principal.getUuid());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(debtReadOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(debtReadOnlyDTO);
    }

    @PutMapping("/{debtUuid}")
    public ResponseEntity<DebtReadOnlyDTO> updateDebt(
            @PathVariable UUID debtUuid,
            @Valid @RequestBody DebtUpdateDTO updateDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal User principal
            ) {

        if (bindingResult.hasErrors()) {
            throw new ValidationException("Debt", "Invalid Debt data", bindingResult);
        }

        DebtReadOnlyDTO debtReadOnlyDTO = debtService.updateDebt(updateDTO, debtUuid, principal.getUuid());
        return ResponseEntity.ok(debtReadOnlyDTO);
    }

    @DeleteMapping("/{debtUuid}")
    public ResponseEntity<DebtReadOnlyDTO> deleteDebt(
            @PathVariable UUID debtUuid,
            @AuthenticationPrincipal User principal
    ) {

        DebtReadOnlyDTO debtReadOnlyDTO = debtService.deleteDebt(debtUuid, principal.getUuid());
        return ResponseEntity.ok(debtReadOnlyDTO);
    }

    @PatchMapping("/{debtUuid}")
    public ResponseEntity<DebtReadOnlyDTO> toggleDebtStatus(
            @PathVariable UUID debtUuid,
            @AuthenticationPrincipal User principal
    ) {

        DebtReadOnlyDTO debtReadOnlyDTO = debtService.toggleStatus(debtUuid, principal.getUuid());
        return ResponseEntity.ok(debtReadOnlyDTO);
    }

    @GetMapping("/{debtUuid}")
    public ResponseEntity<DebtReadOnlyDTO> getDebtByUuid(
            @PathVariable UUID debtUuid,
            @AuthenticationPrincipal User principal) {

        return ResponseEntity.ok(debtService.getDebtByUuid(debtUuid, principal.getUuid()));
    }

    @GetMapping
    public ResponseEntity<Page<DebtReadOnlyDTO>> getPaginatedFilteredDebts(
            @AuthenticationPrincipal User principal,
            @ModelAttribute DebtFilters filters,
            @PageableDefault(sort = "debtorName", direction = Sort.Direction.ASC) Pageable pageable
    ) {

        Page<DebtReadOnlyDTO> debtPage = debtService.getFilteredPaginatedDebts(principal.getUuid(), filters, pageable);
        return ResponseEntity.ok(debtPage);
    }
}
