package ru.safonoviv.oidclmsboi.lms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.safonoviv.oidclmsboi.lms.entities.User;
import ru.safonoviv.oidclmsboi.lms.exceptions.NotFoundException;
import ru.safonoviv.oidclmsboi.lms.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<User> loadByUsername(String email) {
        return userRepository.findByEmail(email);
    }


    @Cacheable(value = "userCache", key = "#name")
    public User findByUsername(String name) throws NotFoundException {
        return userRepository.findByName(name).orElseGet(() -> null);
    }

    @CacheEvict(value = "userCache", key = "#name")
    public void evictUserByUsername(String name) throws NotFoundException {
    }

    @CachePut(value = "userCache", key = "#user.name")
    public User updateUserByUsername(User user) {
        return user;
    }

    @Transactional
    public User createNewUser(OAuth2AuthenticationToken token) {
        return userRepository.save(User.builder()
                .name(token.getName())
                .email((String) token.getPrincipal().getAttributes().get("email"))
                .build());
    }
}
