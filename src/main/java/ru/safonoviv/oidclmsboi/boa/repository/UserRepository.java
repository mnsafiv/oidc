package ru.safonoviv.oidclmsboi.boa.repository;

import org.springframework.data.repository.CrudRepository;
import ru.safonoviv.oidclmsboi.boa.entity.User;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String name);
}
