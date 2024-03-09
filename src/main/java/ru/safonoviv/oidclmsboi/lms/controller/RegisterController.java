package ru.safonoviv.oidclmsboi.lms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.safonoviv.oidclmsboi.lms.entities.BookFeedback;
import ru.safonoviv.oidclmsboi.lms.entities.RegisterBookReserve;
import ru.safonoviv.oidclmsboi.lms.service.RegisterService;

@RestController
@RequestMapping("/v1/bookreg")
@RequiredArgsConstructor
public class RegisterController {
    @Autowired
    private RegisterService registerServiceImpl;

    @PostMapping("/create")
    public ResponseEntity<?> registerReservationBook(@RequestBody RegisterBookReserve registerBookReserve, final OAuth2AuthenticationToken token) {
        return registerServiceImpl.registerReservationBook(registerBookReserve, token);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> registerConfirmBook(@PathVariable("id") Long registerId, final OAuth2AuthenticationToken token) {
        return registerServiceImpl.registerConfirmBook(registerId, token);
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<?> registerClosedBook(@PathVariable("id") Long registerId) {
        return registerServiceImpl.registerReturnBook(registerId);
    }

    @PostMapping("/{id}/feedback")
    public ResponseEntity<?> registerFeedback(@PathVariable("id") Long registerId, @RequestBody BookFeedback bookFeedback, final OAuth2AuthenticationToken token) {
        return registerServiceImpl.registerFeedback(bookFeedback, token);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchGenre(@RequestParam(name = "search", required = false) String search,
                                         @RequestParam(name = "available", defaultValue = "false", required = false) boolean available,
                                         @PageableDefault(value = 3, page = 1)
                                         @SortDefault.SortDefaults({@SortDefault(sort = "id", direction = Sort.Direction.ASC)}) Pageable pageable,
                                         final OAuth2AuthenticationToken token) {
        return registerServiceImpl.availableBook(search, available, pageable, token);
    }


}
