package gr.pants.tdebt.service;

import gr.pants.tdebt.dto.user_dto.UserInsertDTO;
import gr.pants.tdebt.dto.user_dto.UserReadOnlyDTO;
import gr.pants.tdebt.dto.user_dto.UserUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IUserService {

    UserReadOnlyDTO saveUser(UserInsertDTO insertDTO);

    UserReadOnlyDTO updateUser(UUID uuid, UserUpdateDTO updateDTO);

    UserReadOnlyDTO deleteUser(UUID uuid);

    UserReadOnlyDTO getUserByUuid(UUID uuid);

    UserReadOnlyDTO getUserByUuidDeletedFalse(UUID uuid);

    Page<UserReadOnlyDTO> getAllUsers(Pageable pageable);

    Page<UserReadOnlyDTO> getAllUsersDeletedFalse(Pageable pageable);

    boolean isUserExists(String email);
}
