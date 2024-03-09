package ru.safonoviv.oidclmsboi.lms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.safonoviv.oidclmsboi.lms.dto.AuthorDto;
import ru.safonoviv.oidclmsboi.lms.service.AuthorService;

@RestController
@RequestMapping("/v1/author")
@RequiredArgsConstructor
public class AuthorController {
    @Autowired
    private AuthorService authorService;

    @PostMapping("/create")
    public ResponseEntity<?> createAuthor(@RequestBody AuthorDto authorRequest, final OAuth2AuthenticationToken token) {
        return authorService.saveAuthor(authorRequest, token.getName());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchAuthor(@RequestParam(name = "author", required = false) String author,
                                          @PageableDefault(value = 3,page = 1)
                                          @SortDefault.SortDefaults({@SortDefault(sort = "id", direction = Sort.Direction.ASC)}) Pageable pageable) {
        return ResponseEntity.ok(authorService.findAllAuthor(author,pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAuthor(@PathVariable("id") Long id, @RequestBody AuthorDto authorDto, final OAuth2AuthenticationToken token) {
        return authorService.updateAuthor(id, authorDto, token.getName());
    }


}
