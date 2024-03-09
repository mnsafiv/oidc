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
import ru.safonoviv.oidclmsboi.lms.dto.BookDto;
import ru.safonoviv.oidclmsboi.lms.service.BookService;

@RestController
@RequestMapping("/v1/book")
@RequiredArgsConstructor
public class BookController {
    @Autowired
    private BookService bookService;

    @PostMapping("/create")
    public ResponseEntity<?> createBook(@RequestBody BookDto bookDto, final OAuth2AuthenticationToken token) {
        return bookService.saveBook(bookDto, token);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBook(@PathVariable("id") Long id, @RequestBody BookDto bookDto, final OAuth2AuthenticationToken token) {
        return bookService.updateBook(id, bookDto, token);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchBook(@RequestParam(name = "book", required = false) String book,
                                        @RequestParam(name = "available", defaultValue = "true") boolean available,
                                        @PageableDefault(value = 3, page = 1)
                                        @SortDefault.SortDefaults({@SortDefault(sort = "id", direction = Sort.Direction.ASC)}) Pageable pageable) {
        return ResponseEntity.ok(bookService.findAllBook(book, available, pageable));
    }


}
