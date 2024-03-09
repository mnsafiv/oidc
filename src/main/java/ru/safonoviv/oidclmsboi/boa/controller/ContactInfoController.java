package ru.safonoviv.oidclmsboi.boa.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.safonoviv.oidclmsboi.boa.dto.ContactInfoDto;
import ru.safonoviv.oidclmsboi.boa.service.UserContactService;

import java.security.Principal;

@AllArgsConstructor
@RestController
@RequestMapping("/v1/contactInfo")
public class ContactInfoController {
    private final UserContactService userContactService;

    @PostMapping("/create")
    public ResponseEntity<?> addContactInfo(@RequestBody ContactInfoDto contactInfoDto, final Principal principal) {
        return userContactService.addContactInfo(principal.getName(), contactInfoDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editContactInfo(@PathVariable Long id, @RequestBody ContactInfoDto contactInfoDto, final Principal principal) {
        return userContactService.updateContactInfo(id,principal.getName(), contactInfoDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContactInfo(@PathVariable Long id, final Principal principal) {
        return userContactService.removeContactInfoById(principal.getName(), id);
    }

}
