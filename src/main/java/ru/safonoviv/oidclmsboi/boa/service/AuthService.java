package ru.safonoviv.oidclmsboi.boa.service;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import ru.safonoviv.oidclmsboi.boa.dto.RegistrationUserDto;
import ru.safonoviv.oidclmsboi.boa.dto.RegistrationUserFullDto;
import ru.safonoviv.oidclmsboi.boa.entity.User;
import ru.safonoviv.oidclmsboi.boa.keycloaksearch.KeyCloakAdminService;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final KeyCloakAdminService adminService;

    public ResponseEntity<?> createUser(@RequestBody RegistrationUserDto registrationUserDto) {
        if(!adminService.userIsPresent(registrationUserDto.getUsername())){
            return new ResponseEntity<>("Аккаунт не существует", HttpStatus.BAD_REQUEST);
        }
        if (!userService.isAvailableContacts(registrationUserDto.getContacts())) {
            return new ResponseEntity<>("Данные контакты уже заняты!", HttpStatus.BAD_REQUEST);
        }
        User user = userService.createNewUser(registrationUserDto);
        return ResponseEntity.ok(userService.updateUserByUsername(user));
    }


    public ResponseEntity<?> createVerifiedUser(@RequestBody Collection<RegistrationUserFullDto> registrationUsers) {
        if(!adminService.containsUsernames(registrationUsers.stream().map(RegistrationUserFullDto::getUsername).toList())){
            return new ResponseEntity<>("Аккаунт не существует", HttpStatus.BAD_REQUEST);
        }
        userService.createVerifiedUsers(registrationUsers);
        return ResponseEntity.ok("Success");
    }
}
