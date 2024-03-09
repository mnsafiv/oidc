package ru.safonoviv.oidclmsboi.boa.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.safonoviv.oidclmsboi.boa.entity.User;
import ru.safonoviv.oidclmsboi.boa.service.UserContactService;
import ru.safonoviv.oidclmsboi.boa.service.UserService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/v1/clients")
public class ClientController {

    private final UserService userService;
    private final UserContactService userContactService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchClient(@RequestParam(name = "contact",required = false) String contact,
                                          @RequestParam(name = "fullName",required = false) String fullName,
                                          @RequestParam(name = "date",required = false) LocalDate date,
                                          @PageableDefault(value = 3)
                                          @SortDefault.SortDefaults({@SortDefault(sort = "id", direction = Sort.Direction.ASC)}) Pageable pageable) {
        Collection<Long> search = userContactService.findBySearch(contact, fullName, date, pageable);
        Set<User> users = search.stream().map(userService::getUserById).collect(Collectors.toSet());
        return ResponseEntity.ok(search.stream().map(userService::getUserById).collect(Collectors.toSet()));
    }
}
