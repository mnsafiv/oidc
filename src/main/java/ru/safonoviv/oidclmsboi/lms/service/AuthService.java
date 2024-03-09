package ru.safonoviv.oidclmsboi.lms.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import ru.safonoviv.oidclmsboi.lms.entities.User;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;

    public User createNewUser(OAuth2AuthenticationToken token) {
        if (userService.findByUsername(token.getName()) != null) {
            throw new RuntimeException("User exist!");
        }
        User newUser = userService.createNewUser(token);
        return userService.updateUserByUsername(newUser);
    }
}
