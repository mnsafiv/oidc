package ru.safonoviv.oidclmsboi.boa.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import ru.safonoviv.oidclmsboi.boa.dto.RegistrationUserDto;
import ru.safonoviv.oidclmsboi.boa.dto.RegistrationUserFullDto;
import ru.safonoviv.oidclmsboi.boa.entity.*;
import ru.safonoviv.oidclmsboi.boa.exceptions.SqlRollback;
import ru.safonoviv.oidclmsboi.boa.exceptions.NotFoundException;
import ru.safonoviv.oidclmsboi.boa.repository.UserRepository;
import ru.safonoviv.oidclmsboi.boa.util.SearchUtil;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService  {
    @PersistenceContext
    private EntityManager entityManager;
    private final UserRepository userRepository;
    private final SearchUtil searchUtil;


    public Optional<User> findByUsername(String name) {
        return userRepository.findByUsername(name);
    }

    @Cacheable(value = "userCache", key = "#username")
    public User getByUsername(String username) throws NotFoundException {
        return userRepository.findByUsername(username).orElseGet(() -> null);
    }

    @CacheEvict(value = "userCache", key = "#username")
    public void evictUserByUsername(String username)  {
    }

    @CachePut(value = "userCache", key = "#user.username")
    public User updateUserByUsername(User user) {
        return user;
    }

    @CachePut(value = "userCache", key = "#user.name")
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found!", HttpStatus.BAD_REQUEST));
    }

    @CacheEvict(value = "userCache", allEntries = true)
    public void evictAllUserCache() {
    }


    @Transactional
    public User createNewUser(OAuth2AuthenticationToken token) {
        return userRepository.save(User.builder()
                .username(token.getName())
                .build());
    }

    public boolean isAvailableContacts(Collection<String> contacts) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserContact> cr = cb.createQuery(UserContact.class);
        Root<UserContact> userContactRoot = cr.from(UserContact.class);
        CriteriaQuery<UserContact> userContacts = cr.where(cb.in(userContactRoot.get("contactInfo")).value(contacts));
        return entityManager.createQuery(userContacts).getResultList().isEmpty();
    }

    public boolean isCorrectContact(Collection<String> contacts) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserContact> cr = cb.createQuery(UserContact.class);
        Root<UserContact> userContactRoot = cr.from(UserContact.class);
        CriteriaQuery<UserContact> userContacts = cr.where(cb.in(userContactRoot.get("contactInfo")).value(contacts));
        return contacts.size() == entityManager.createQuery(userContacts).getResultList().size();
    }

    @Transactional(rollbackOn = SqlRollback.class)
    public User createNewUser(RegistrationUserDto registrationUserDto) {
        User user = User.builder()
                .username(registrationUserDto.getUsername())
                .build();
        ClientAccount clientAccount = ClientAccount.builder()
                .user(user)
                .balanceCurrent(registrationUserDto.getBalance())
                .balanceStart(registrationUserDto.getBalance())
                .build();
        Set<UserContact> userContacts = registrationUserDto.getContacts().stream().map(searchUtil::getContact).collect(Collectors.toSet());
        for (UserContact userContact : userContacts) userContact.setUser(user);
        UserInfo userInfo = UserInfo.builder()
                .user(user)
                .build();

        user.setUserContact(userContacts);
        user.setClientAccount(clientAccount);
        user.setUserInfo(userInfo);
        userRepository.save(user);
        System.out.println();
        if (user.getId() == null || !isCorrectContact(registrationUserDto.getContacts())) {
            throw new SqlRollback("Акаунт не создан!", HttpStatus.BAD_REQUEST);
        }
        return user;
    }




    @Transactional(rollbackOn = SqlRollback.class)
    public void createVerifiedUsers(Collection<RegistrationUserFullDto> registrationUsers) {
        Set<String> contacts = registrationUsers.stream().flatMap(n -> n.getContact().stream()).collect(Collectors.toSet());
        if (!isAvailableContacts(contacts)) {
            throw new SqlRollback("Аккаунт не создан! Не уникальная контактная информация!", HttpStatus.BAD_REQUEST);
        }
        for (RegistrationUserFullDto regUser : registrationUsers) {
            User user = User.builder()
                    .username(regUser.getUsername())
                    .build();
            ClientAccount clientAccount = ClientAccount.builder()
                    .user(user)
                    .balanceCurrent(regUser.getBalance())
                    .balanceStart(regUser.getBalance())
                    .verified(true)
                    .build();
            user.setClientAccount(clientAccount);

            List<UserContact> userContacts = regUser.getContact().stream().map(searchUtil::getContact).toList();
            for (UserContact userContact : userContacts) userContact.setUser(user);

            user.setUserContact(new HashSet<>(userContacts));
            UserInfo userInfo = UserInfo.builder()
                    .user(user)
                    .firstName(regUser.getFirstName())
                    .middleName(regUser.getMiddleName())
                    .secondName(regUser.getSecondName())
                    .dateOfBirth(regUser.getDateOfBorn())
                    .build();
            user.setUserInfo(userInfo);
            userRepository.save(user);
            if (user.getId() == null) {
                throw new SqlRollback("Аккаунт не создан! " + user.getUsername(), HttpStatus.BAD_REQUEST);
            }
        }
        if (!isCorrectContact(contacts)) {
            throw new SqlRollback("Аккаунт не создан из-за дубликатов в контактах!", HttpStatus.BAD_REQUEST);
        }
    }
}
