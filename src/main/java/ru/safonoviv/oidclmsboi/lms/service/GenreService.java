package ru.safonoviv.oidclmsboi.lms.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import ru.safonoviv.oidclmsboi.lms.entities.Genre;
import ru.safonoviv.oidclmsboi.lms.exceptions.ExceptionCustomRollback;
import ru.safonoviv.oidclmsboi.lms.exceptions.NotFoundException;

public interface GenreService {

    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    ResponseEntity<?> saveGenre(Genre genre, OAuth2AuthenticationToken token) throws ExceptionCustomRollback;

    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    ResponseEntity<?> updateGenre(Long id, Genre genre, OAuth2AuthenticationToken token) throws ExceptionCustomRollback;

    ResponseEntity<?> findGenre(String genre, Pageable pageable) throws NotFoundException;
}
