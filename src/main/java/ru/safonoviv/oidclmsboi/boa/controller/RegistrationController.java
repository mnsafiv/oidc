package ru.safonoviv.oidclmsboi.boa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.safonoviv.oidclmsboi.boa.dto.RegistrationUserDto;
import ru.safonoviv.oidclmsboi.boa.dto.RegistrationUserFullDto;
import ru.safonoviv.oidclmsboi.boa.service.AuthService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
public class RegistrationController {
	private final AuthService authService;
	@PostMapping("/registration")
	public ResponseEntity<?> createNewUser(@RequestBody RegistrationUserDto registrationUserDto) {
		return authService.createUser(registrationUserDto);
	}
	@PostMapping("/registration-full")
	public ResponseEntity<?> createNewUserFull(@RequestBody Collection<RegistrationUserFullDto> registrationUsers) {
		return authService.createVerifiedUser(registrationUsers);
	}
}