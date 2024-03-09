package ru.safonoviv.oidclmsboi.boa.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.safonoviv.oidclmsboi.boa.dto.TransferDepositDto;
import ru.safonoviv.oidclmsboi.boa.service.ClientAccountService;

import java.security.Principal;

@AllArgsConstructor
@RestController
@RequestMapping("/v1/account")
public class ClientAccountController {
    @Autowired
    private ClientAccountService clientAccountService;

    @PostMapping("/transfer")
    public ResponseEntity<?> transferDeposit(@RequestBody TransferDepositDto transferDepositDto, final Principal principal){
        clientAccountService.transferDeposit(transferDepositDto,principal.getName());
        return ResponseEntity.ok("Success!");
    }

}
