package gr.pants.tdebt.api;

import gr.pants.tdebt.core.exceptions.ValidationException;
import gr.pants.tdebt.dto.user_dto.UserInsertDTO;
import gr.pants.tdebt.dto.user_dto.UserReadOnlyDTO;
import gr.pants.tdebt.dto.user_dto.UserUpdateDTO;
import gr.pants.tdebt.service.IUserService;
import gr.pants.tdebt.validators.UserInsertValidator;
import gr.pants.tdebt.validators.UserUpdateValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserRestController {

    private final IUserService userService;
    private final UserInsertValidator insertValidator;
    private final UserUpdateValidator updateValidator;

    @PostMapping
    public ResponseEntity<UserReadOnlyDTO> saveUser(
            @Valid @RequestBody UserInsertDTO userInsertDTO,
            BindingResult bindingResult
            ) {

        insertValidator.validate(userInsertDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new ValidationException("User", "Invalid user data", bindingResult);
        }

        UserReadOnlyDTO userReadOnlyDTO = userService.saveUser(userInsertDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{uuid}")
                .buildAndExpand(userReadOnlyDTO.uuid())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(userReadOnlyDTO);
    }


    @PutMapping("/{uuid}")
    public ResponseEntity<UserReadOnlyDTO> updateUser(
            @PathVariable UUID uuid,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO,
            BindingResult bindingResult
            ) {

        updateValidator.validate(userUpdateDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new ValidationException("User", "Invalid user data", bindingResult);
        }

        UserReadOnlyDTO userReadOnlyDTO = userService.updateUser(uuid, userUpdateDTO);
        return ResponseEntity.ok(userReadOnlyDTO);
    }


    @DeleteMapping("/{uuid}")
    public ResponseEntity<UserReadOnlyDTO> deleteUser(
            @PathVariable UUID uuid
    ) {

        UserReadOnlyDTO userReadOnlyDTO = userService.deleteUser(uuid);
        return ResponseEntity.ok(userReadOnlyDTO);
    }


    @GetMapping("/{uuid}")
    public ResponseEntity<UserReadOnlyDTO> getUserByUuid(
            @PathVariable UUID uuid,
            @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {

        UserReadOnlyDTO userReadOnlyDTO = includeDeleted ?
                userService.getUserByUuid(uuid) :
                userService.getUserByUuidDeletedFalse(uuid);

        return ResponseEntity.ok(userReadOnlyDTO);
    }


    @GetMapping
    public ResponseEntity<Page<UserReadOnlyDTO>> getPaginatedUsers(
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @PageableDefault(size = 10, sort = "email") Pageable pageable
    ) {

        Page<UserReadOnlyDTO> usersPaginated = includeDeleted ?
                userService.getAllUsers(pageable) :
                userService.getAllUsersDeletedFalse(pageable);

        return ResponseEntity.ok(usersPaginated);
    }
}
