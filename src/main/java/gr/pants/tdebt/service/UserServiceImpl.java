package gr.pants.tdebt.service;

import gr.pants.tdebt.dto.user_dto.UserInsertDTO;
import gr.pants.tdebt.dto.user_dto.UserReadOnlyDTO;
import gr.pants.tdebt.dto.user_dto.UserUpdateDTO;
import gr.pants.tdebt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    // private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public UserReadOnlyDTO saveUser(UserInsertDTO insertDTO) {
        return null;
    }

    @Override
    public UserReadOnlyDTO updateUser(UserUpdateDTO updateDTO) {
        return null;
    }

    @Override
    public void deleteUser(UUID uuid) {

    }

    @Override
    public UserReadOnlyDTO getUserByUuid(UUID uuid) {
        return null;
    }

    @Override
    public UserReadOnlyDTO getUserByUuidDeletedFalse(UUID uuid) {
        return null;
    }

    @Override
    public Page<UserReadOnlyDTO> getAllUsers(Pageable pageable) {
        return null;
    }

    @Override
    public Page<UserReadOnlyDTO> getAllUsersDeletedFalse(Pageable pageable) {
        return null;
    }

    @Override
    public boolean isUserExists(String email) {
        return false;
    }
}
