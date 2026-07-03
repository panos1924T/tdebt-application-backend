package gr.pants.tdebt.service;

import gr.pants.tdebt.core.exceptions.EntityAlreadyExistsException;
import gr.pants.tdebt.core.exceptions.EntityNotFoundException;
import gr.pants.tdebt.core.exceptions.PasswordMismatchException;
import gr.pants.tdebt.dto.user_dto.UserInsertDTO;
import gr.pants.tdebt.dto.user_dto.UserReadOnlyDTO;
import gr.pants.tdebt.dto.user_dto.UserUpdateDTO;
import gr.pants.tdebt.mapper.UserMapper;
import gr.pants.tdebt.model.Role;
import gr.pants.tdebt.model.User;
import gr.pants.tdebt.repository.RoleRepository;
import gr.pants.tdebt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Transactional
    @Override
    public UserReadOnlyDTO saveUser(UserInsertDTO insertDTO) {

        if (userRepository.existsUserByEmail(insertDTO.email())) {
            throw new EntityAlreadyExistsException("User", "Email " + insertDTO.email() + " already exists");
        }
        if (!insertDTO.password().equals(insertDTO.confirmPassword())) {
            throw new PasswordMismatchException("User", "Passwords do not match");
        }

        String hashedPassword = passwordEncoder.encode(insertDTO.password());
        User user = userMapper.toEntity(insertDTO, hashedPassword);
        Role role = roleRepository.findRoleByName("USER")
                .orElseThrow(() -> new EntityNotFoundException("Role", "Role=USER was not found"));
        user.setRole(role);
        User savedUser = userRepository.save(user);

        log.info("User with email={} saved successfully.", insertDTO.email());
        return userMapper.toReadOnlyDTO(savedUser);
    }

    @PreAuthorize("hasAuthority('EDIT_ONLY_USER') and #uuid == authentication.principal.uuid")
    @Transactional
    @Override
    public UserReadOnlyDTO updateUser(UUID uuid, UserUpdateDTO updateDTO) {

        User user = userRepository.findUserByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new EntityNotFoundException("User", "User with uuid=" + uuid + " not found"));

        if (userRepository.existsUserByEmailAndUuidNot(updateDTO.email(), uuid)) {
            throw new EntityAlreadyExistsException("User", "Email " + updateDTO.email() + " already exists");
        }
        if (!updateDTO.password().equals(updateDTO.confirmPassword())) {
            throw new PasswordMismatchException("User", "Passwords do not match");
        }

        user.setEmail(updateDTO.email());
        user.setPassword(passwordEncoder.encode(updateDTO.password()));

        log.info("User with uuid={} updated successfully", uuid);
        return userMapper.toReadOnlyDTO(user);
    }

    @PreAuthorize("hasAuthority('DELETE_USER') or " +
            "hasAuthority('DELETE_ONLY_USER') and #uuid == authentication.principal.uuid")
    @Transactional
    @Override
    public UserReadOnlyDTO deleteUser(UUID uuid) {

        User user = userRepository.findUserByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new EntityNotFoundException("User", "User with uuid=" + uuid + " not found"));

        user.softDelete(Instant.now());
        log.info("User with uuid={} deleted successfully", uuid);
        return userMapper.toReadOnlyDTO(user);
    }

    @PreAuthorize("hasAuthority('VIEW_USERS')")
    @Transactional(readOnly = true)
    @Override
    public UserReadOnlyDTO getUserByUuid(UUID uuid) {

        User user = userRepository.findUserByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("User", "User with uuid=" + uuid + " not found"));

        log.info("Get user by uuid={} returned successfully", uuid);
        return userMapper.toReadOnlyDTO(user);
    }

    @PreAuthorize("hasAuthority('VIEW_USERS') or " +
            "hasAuthority('VIEW_ONLY_USER') and #uuid == authentication.principal.uuid")
    @Transactional(readOnly = true)
    @Override
    public UserReadOnlyDTO getUserByUuidDeletedFalse(UUID uuid) {
        User user = userRepository.findUserByUuidAndDeletedFalse(uuid)
                .orElseThrow(() -> new EntityNotFoundException("User", "User with uuid=" + uuid + " not found"));

        log.debug("Get non-deleted user by uuid={} returned successfully", uuid);
        return userMapper.toReadOnlyDTO(user);
    }

    @PreAuthorize("hasAuthority('VIEW_USERS')")
    @Transactional(readOnly = true)
    @Override
    public Page<UserReadOnlyDTO> getAllUsers(Pageable pageable) {

        Page<User> userPage = userRepository.findAll(pageable);

        log.info("Get paginated returned successfully page={} and size={}", userPage.getNumber(), userPage.getSize());
        return userPage.map(userMapper::toReadOnlyDTO);
    }

    @PreAuthorize("hasAuthority('VIEW_USERS')")
    @Transactional(readOnly = true)
    @Override
    public Page<UserReadOnlyDTO> getAllUsersDeletedFalse(Pageable pageable) {

        Page<User> userPage = userRepository.findAllByDeletedFalse(pageable);

        log.info("Get paginated not deleted returned successfully page={} and size={}", userPage.getNumber(), userPage.getSize());
        return userPage.map(userMapper::toReadOnlyDTO);
    }

    @Override
    public boolean isUserExists(String email) {
        return userRepository.existsUserByEmail(email);
    }
}
