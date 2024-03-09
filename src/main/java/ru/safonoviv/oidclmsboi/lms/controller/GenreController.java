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
import ru.safonoviv.oidclmsboi.lms.entities.Genre;
import ru.safonoviv.oidclmsboi.lms.service.GenreService;

@RestController
@RequestMapping("/v1/genre")
@RequiredArgsConstructor
public class GenreController {

    @Autowired
    private GenreService genreService;

    @PostMapping("/create")
    public ResponseEntity<?> createGenre(@RequestBody Genre genre, final OAuth2AuthenticationToken token) {
        return genreService.saveGenre(genre, token);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGenre(@PathVariable("id") Long id, @RequestBody Genre genre, final OAuth2AuthenticationToken token) {
        return genreService.updateGenre(id, genre, token);
    }

    @GetMapping("search")
    public ResponseEntity<?> searchGenre(@RequestParam(name = "genre", required = false) String genre,
                                         @PageableDefault(value = 3, page = 1)
                                         @SortDefault.SortDefaults({@SortDefault(sort = "id", direction = Sort.Direction.ASC)}) Pageable pageable) {
        return genreService.findGenre(genre,pageable);
    }


}
