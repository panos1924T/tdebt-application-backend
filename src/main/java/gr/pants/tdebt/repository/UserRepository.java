package gr.pants.tdebt.repository;

import gr.pants.tdebt.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByEmailAndDeletedFalse(String email);

    Optional<User> findUserByEmail(String email);

    boolean existsUserByRole(String name);
}
