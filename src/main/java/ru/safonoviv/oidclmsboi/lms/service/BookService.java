package ru.safonoviv.oidclmsboi.lms.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import ru.safonoviv.oidclmsboi.lms.dto.BookDto;
import ru.safonoviv.oidclmsboi.lms.exceptions.ExceptionCustomRollback;
import ru.safonoviv.oidclmsboi.lms.exceptions.NotFoundException;

public interface BookService {
    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    ResponseEntity<?> saveBook(BookDto bookDto, OAuth2AuthenticationToken token) throws ExceptionCustomRollback;

    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    ResponseEntity<?> updateBook(Long id, BookDto bookDto, OAuth2AuthenticationToken token) throws ExceptionCustomRollback;

    ResponseEntity<?> findAllBook(String book, boolean available, Pageable pageable) throws NotFoundException;
}
