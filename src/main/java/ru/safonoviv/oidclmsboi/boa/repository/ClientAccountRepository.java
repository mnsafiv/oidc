package ru.safonoviv.oidclmsboi.boa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.safonoviv.oidclmsboi.boa.entity.ClientAccount;

@Repository
public interface ClientAccountRepository extends CrudRepository<ClientAccount, Long> {
}
