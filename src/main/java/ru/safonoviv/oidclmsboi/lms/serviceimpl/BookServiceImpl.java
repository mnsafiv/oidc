package ru.safonoviv.oidclmsboi.lms.serviceimpl;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import ru.safonoviv.oidclmsboi.lms.dto.*;
import ru.safonoviv.oidclmsboi.lms.entities.*;
import ru.safonoviv.oidclmsboi.lms.exceptions.ExceptionCustomRollback;
import ru.safonoviv.oidclmsboi.lms.exceptions.NotFoundException;
import ru.safonoviv.oidclmsboi.lms.repository.BookRepository;
import ru.safonoviv.oidclmsboi.lms.repository.GenreRepository;
import ru.safonoviv.oidclmsboi.lms.service.BookService;
import ru.safonoviv.oidclmsboi.lms.service.UserService;
import ru.safonoviv.oidclmsboi.lms.util.SortUtil;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepo;
    private final GenreRepository genreRepo;
    private final SortUtil sortUtil;
    private final UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    public ResponseEntity<?> saveBook(BookDto bookDto, OAuth2AuthenticationToken token) throws ExceptionCustomRollback {
        User user = userService.findByUsername(token.getName());
        if (user == null) {
            throw new NotFoundException(String.format("Not found user: %s", token.getName()), HttpStatus.BAD_REQUEST);
        }
        Book book = bookDto.convertToBook();
        book.setUserCreated(user);
        book.setDateCreated(LocalDate.now());
        entityManager.persist(book);

        if (book.getId() != null) {
            return ResponseEntity.ok(String.format("Книга сохранена id: %s, название: %s, описание: %s", book.getId(), book.getName(), book.getDescription()));
        }
        throw new ExceptionCustomRollback("Не удалось сохранить", HttpStatus.BAD_REQUEST);
    }

    @Override
    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    public ResponseEntity<?> updateBook(Long id, BookDto updateBookDto, OAuth2AuthenticationToken token) throws ExceptionCustomRollback, NotFoundException {
        User user = userService.findByUsername(token.getName());
        if (user == null) {
            throw new NotFoundException(String.format("Not found user: %s", token.getName()), HttpStatus.BAD_REQUEST);
        }
        updateBookDto.setBookId(id);
        Book updateBook = updateBookDto.convertToBook();

        Optional<Book> optionalExistBook = bookRepo.findById(id);
        if (optionalExistBook.isPresent() && optionalExistBook.get().getUserCreated().equals(user)) {
            Book existBook = optionalExistBook.get();

            Set<AuthorBook> removeAuthors = updateBook.getAuthorBooks().stream().filter(t -> !existBook.getAuthorBooks().contains(t)).collect(Collectors.toSet());
            Set<GenreBook> removeGenre = updateBook.getGenreBooks().stream().filter(t -> !existBook.getGenreBooks().contains(t)).collect(Collectors.toSet());
            Set<AuthorBook> addAuthors = existBook.getAuthorBooks().stream().filter(t -> !updateBook.getAuthorBooks().contains(t)).collect(Collectors.toSet());
            Set<GenreBook> addGenre = existBook.getGenreBooks().stream().filter(t -> !updateBook.getGenreBooks().contains(t)).collect(Collectors.toSet());

            existBook.setDescription(updateBook.getDescription());
            existBook.setName(updateBook.getName());
            existBook.setYear(updateBook.getYear());
            existBook.setAvailable(updateBook.isAvailable());
            existBook.getGenreBooks().removeAll(removeGenre);
            existBook.getAuthorBooks().removeAll(removeAuthors);
            existBook.getGenreBooks().addAll(addGenre);
            existBook.getAuthorBooks().addAll(addAuthors);

            try {
                entityManager.merge(existBook);
            } catch (IllegalArgumentException e) {
                throw new ExceptionCustomRollback(e.getMessage(), HttpStatus.BAD_REQUEST);
            }

        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> findAllBook(String book, boolean available, Pageable pageable) throws NotFoundException {
        if (pageable.getPageNumber() < 1) {
            throw new NotFoundException("Bad request!", HttpStatus.BAD_REQUEST);
        }
        sortUtil.checkSortParameters(pageable.getSort(), Book.class.getDeclaredFields());

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BookDto> cr = cb.createQuery(BookDto.class);
        Root<Book> bookRoot = cr.from(Book.class);
        List<Predicate> predicates = new ArrayList<>();
        if (book != null) {
            predicates.add(cb.like(bookRoot.get("name"), "%" + book + "%"));
        }
        if (available) {
            predicates.add(cb.equal(bookRoot.get("isAvailable"), true));
        }

        List<Order> sort = sortUtil.getSort(pageable.getSort(), bookRoot, cb);

        Join<AuthorBook, Author> author = bookRoot.join("authorBooks", JoinType.LEFT).join("author", JoinType.LEFT);
        Join<GenreBook, Genre> genre = bookRoot.join("genreBooks", JoinType.LEFT).join("genre", JoinType.LEFT);

        Expression<String> concatWsAuthor = cb.function("CONCAT_WS", String.class,
                cb.literal(";"),
                author.get("id"),
                author.get("name"));

        Expression<String> concatWsGenre = cb.function("CONCAT_WS", String.class,
                cb.literal(";"),
                genre.get("id"),
                genre.get("name"));

        Expression<String> distinctAuthor = cb.function("DISTINCT", String.class, concatWsAuthor);
        Expression<String> distinctGenre = cb.function("DISTINCT", String.class, concatWsGenre);

        cr.where(cb.and(predicates.toArray(new Predicate[0])));
        cr.orderBy(sort);
        cr.groupBy(bookRoot.get("id"));


        CriteriaQuery<BookDto> select = cr.multiselect(
                bookRoot.get("id"),
                bookRoot.get("name"),
                bookRoot.get("description"),
                bookRoot.get("isAvailable"),
                bookRoot.get("year"),
                bookRoot.get("dateCreated"),
                bookRoot.get("userCreated").get("id"),
                cb.function("array_agg", String.class, distinctAuthor),
                cb.function("array_agg", String.class, distinctGenre)
        );

        TypedQuery<BookDto> typedQuery = entityManager.createQuery(select);

        typedQuery.setFirstResult((pageable.getPageNumber() - 1) * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<BookDto> results = typedQuery.getResultList();

        if (results.isEmpty()) {
            throw new NotFoundException("Нет книг или не существует страница", HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(results);
    }


}
