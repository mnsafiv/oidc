package ru.safonoviv.oidclmsboi.boa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationUserFullDto {
    private String username;
    private List<String> contact;
    private float balance;
    private String firstName;
    private String secondName;
    private String middleName;
    private LocalDate dateOfBorn;
}
