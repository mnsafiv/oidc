package ru.safonoviv.oidclmsboi.lms.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import ru.safonoviv.oidclmsboi.lms.dto.AuthorDto;
import ru.safonoviv.oidclmsboi.lms.exceptions.ExceptionCustomRollback;
import ru.safonoviv.oidclmsboi.lms.exceptions.NotFoundException;

public interface AuthorService {

    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    ResponseEntity<?> saveAuthor(AuthorDto authorRequest, String username) throws ExceptionCustomRollback;

    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    ResponseEntity<?> updateAuthor(Long id, AuthorDto authorDto, String username) throws ExceptionCustomRollback;

    ResponseEntity<?> findAllAuthor(String author, Pageable pageable) throws NotFoundException;

}
