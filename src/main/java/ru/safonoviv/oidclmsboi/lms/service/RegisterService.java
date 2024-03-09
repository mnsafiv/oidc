package ru.safonoviv.oidclmsboi.lms.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import ru.safonoviv.oidclmsboi.lms.entities.BookFeedback;
import ru.safonoviv.oidclmsboi.lms.entities.RegisterBookReserve;
import ru.safonoviv.oidclmsboi.lms.exceptions.ExceptionCustomRollback;
import ru.safonoviv.oidclmsboi.lms.exceptions.NotFoundException;

public interface RegisterService {

    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    ResponseEntity<?> registerReservationBook(RegisterBookReserve registerBookReserve, OAuth2AuthenticationToken token) throws ExceptionCustomRollback;


    ResponseEntity<?> myAvailableBook(OAuth2AuthenticationToken token) throws NotFoundException;

    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    ResponseEntity<?> registerReturnBook(long registerId) throws ExceptionCustomRollback;

    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    ResponseEntity<?> registerConfirmBook(long registerId, OAuth2AuthenticationToken token) throws ExceptionCustomRollback;

    ResponseEntity<?> availableBook(String search, boolean available, Pageable pageable, OAuth2AuthenticationToken token) throws NotFoundException;

    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    ResponseEntity<?> registerClosedBook(Long registerId) throws ExceptionCustomRollback;

    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    ResponseEntity<?> registerFeedback(BookFeedback registerId, OAuth2AuthenticationToken token) throws ExceptionCustomRollback;
}
