package ru.safonoviv.oidclmsboi.boa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationUserDto {
    private String username;
    private List<String> contacts;
    private float balance;
}
