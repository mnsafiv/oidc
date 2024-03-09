package ru.safonoviv.oidclmsboi.boa.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.RollbackException;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.safonoviv.oidclmsboi.boa.dto.TransferDepositDto;
import ru.safonoviv.oidclmsboi.boa.entity.ClientAccount;
import ru.safonoviv.oidclmsboi.boa.entity.User;
import ru.safonoviv.oidclmsboi.boa.exceptions.SqlRollback;
import ru.safonoviv.oidclmsboi.boa.repository.ClientAccountRepository;

@Service
@AllArgsConstructor
public class ClientAccountService {
    @PersistenceContext
    private EntityManager entityManager;
    private final UserService userService;
    private final UserContactService userContactService;
    private final ClientAccountRepository clientAccountRepository;


    @Transactional(rollbackFor = RollbackException.class, isolation = Isolation.REPEATABLE_READ)
    public void increaseDeposit(double depositValue, double depositStartMax) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<ClientAccount> criteriaUpdateClientAccount = cb.createCriteriaUpdate(ClientAccount.class);
        Root<ClientAccount> clientAccountRoot = criteriaUpdateClientAccount.from(ClientAccount.class);
        Expression<Double> leastValues = cb.function("LEAST", Double.class,
                cb.prod(clientAccountRoot.get("balanceCurrent"), depositValue),
                cb.prod(clientAccountRoot.get("balanceStart"), depositStartMax));

        Expression<Double> greatestValues = cb.function("GREATEST", Double.class,
                clientAccountRoot.get("balanceCurrent"),
                leastValues);

        criteriaUpdateClientAccount.set("balanceCurrent", greatestValues);

        try {
            entityManager.createQuery(criteriaUpdateClientAccount).executeUpdate();
        } catch (Exception e) {
            throw new RollbackException(e.getMessage(), e.getCause());
        }
        userService.evictAllUserCache();
    }

    @Transactional(rollbackFor = SqlRollback.class, isolation = Isolation.READ_COMMITTED)
    public void transferDeposit(TransferDepositDto transferDepositDto, String senderLogin) {
        Long receiverId = userContactService.findUserIdByEmailOrPhone(transferDepositDto.getReceiver());
        User userSender = userService.getByUsername(senderLogin);

        Long senderId = userSender.getId();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaUpdate<ClientAccount> criteriaUpdateClientAccountSender = cb.createCriteriaUpdate(ClientAccount.class);
        Root<ClientAccount> clientAccountRootSender = criteriaUpdateClientAccountSender.from(ClientAccount.class);
        criteriaUpdateClientAccountSender.set("balanceCurrent", cb.diff(clientAccountRootSender.get("balanceCurrent"), transferDepositDto.getValue()));
        criteriaUpdateClientAccountSender.where(cb.equal(clientAccountRootSender.get("id"), senderId), (cb.equal(clientAccountRootSender.get("verified"), true)));
        if (entityManager.createQuery(criteriaUpdateClientAccountSender).executeUpdate() == 0) {
            throw new SqlRollback("Your account no verified!", HttpStatus.BAD_REQUEST);
        }

        CriteriaUpdate<ClientAccount> criteriaUpdateClientAccountReceiver = cb.createCriteriaUpdate(ClientAccount.class);
        Root<ClientAccount> clientAccountRootReceiver = criteriaUpdateClientAccountReceiver.from(ClientAccount.class);
        criteriaUpdateClientAccountReceiver.set("balanceCurrent", cb.sum(clientAccountRootReceiver.get("balanceCurrent"), transferDepositDto.getValue()));
        criteriaUpdateClientAccountReceiver.where(cb.equal(clientAccountRootReceiver.get("id"), receiverId));
        entityManager.createQuery(criteriaUpdateClientAccountReceiver).executeUpdate();


        CriteriaQuery<Float> selectBalanceReceiver = cb.createQuery(Float.class);
        Root<ClientAccount> clientRoot = selectBalanceReceiver.from(ClientAccount.class);
        selectBalanceReceiver.where(cb.equal(clientRoot.get("id"), senderId));
        selectBalanceReceiver.select(clientRoot.get("balanceCurrent"));

        Float balance = entityManager.createQuery(selectBalanceReceiver).getSingleResult();

        if (balance < 0) {
            throw new SqlRollback("Баланс отрицательный после транзакции: " + balance + " rollback", HttpStatus.BAD_REQUEST);
        }


    }
}
