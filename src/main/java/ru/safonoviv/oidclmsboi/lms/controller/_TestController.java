package ru.safonoviv.oidclmsboi.lms.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.safonoviv.oidclmsboi.lms.entities.User;
import ru.safonoviv.oidclmsboi.lms.service.UserService;
import ru.safonoviv.oidclmsboi.lms.service.AuthService;

import java.util.List;

@AllArgsConstructor
@Controller
public class _TestController {


    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) throws Exception {
        request.logout();
        return "redirect:/myself";
    }

    @GetMapping("/")
    public String home(OAuth2AuthenticationToken token) {
        return "redirect:/myself";
    }

    @GetMapping("/myself")
    public ResponseEntity<?> myself(OAuth2AuthenticationToken token) {
        User user = userService.findByUsername(token.getName());
        if (user == null) {
            return new ResponseEntity<>("User no created: " + token.getName(), HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/createUser")
    public ResponseEntity<?> registerUser(OAuth2AuthenticationToken token) {
        User user = userService.findByUsername(token.getName());
        if (user == null ) {
            user = authService.createNewUser(token);
        }
        return ResponseEntity.ok(user);
    }
}
