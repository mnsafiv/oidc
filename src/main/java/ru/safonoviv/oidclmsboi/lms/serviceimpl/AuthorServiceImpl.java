package ru.safonoviv.oidclmsboi.lms.serviceimpl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import ru.safonoviv.oidclmsboi.lms.dto.AuthorDto;
import ru.safonoviv.oidclmsboi.lms.entities.*;
import ru.safonoviv.oidclmsboi.lms.exceptions.ExceptionCustomRollback;
import ru.safonoviv.oidclmsboi.lms.exceptions.NotFoundException;
import ru.safonoviv.oidclmsboi.lms.repository.AuthorRepository;
import ru.safonoviv.oidclmsboi.lms.service.AuthorService;
import ru.safonoviv.oidclmsboi.lms.service.UserService;
import ru.safonoviv.oidclmsboi.lms.util.SortUtil;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    @PersistenceContext
    private EntityManager entityManager;
    private final UserService userService;
    private final SortUtil sortUtil;

    private final AuthorRepository authorRepo;

    @Override
    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    public ResponseEntity<?> saveAuthor(AuthorDto authorDto, String username) throws ExceptionCustomRollback {
        User user;
        if ((user = userService.findByUsername(username)) == null) {
            throw new NotFoundException(String.format("User not found: %s!", username), HttpStatus.BAD_REQUEST);
        }
        Author author = new Author(null, authorDto.getAuthorName(), user, null);
        author = authorRepo.save(author);
        if (author.getId() != null) {
            return ResponseEntity.ok(author.getId() + " " + author.getName());
        }

        throw new ExceptionCustomRollback("Не удалось сохранить", HttpStatus.BAD_REQUEST);
    }

    @Override
    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    public ResponseEntity<?> updateAuthor(Long id, AuthorDto authorDto, String username) throws ExceptionCustomRollback {
        User user = userService.findByUsername(username);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<Author> criteriaUpdateBook = cb.createCriteriaUpdate(Author.class);
        Root<Author> rootBook = criteriaUpdateBook.from(Author.class);
        criteriaUpdateBook.set("name", authorDto.getAuthorName());
        criteriaUpdateBook.where(cb.equal(rootBook.get("id"), id), cb.equal(rootBook.get("userCreated").get("id"), user.getId()));
        if (entityManager.createQuery(criteriaUpdateBook).executeUpdate() == 1) {
            return ResponseEntity.ok("Обновлено");
        }
        throw new ExceptionCustomRollback("Bad request!", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> findAllAuthor(String author, Pageable pageable) throws NotFoundException {
        if (pageable.getPageNumber() < 1) {
            throw new NotFoundException("Bad request!", HttpStatus.BAD_REQUEST);
        }
        sortUtil.checkSortParameters(pageable.getSort(), Book.class.getDeclaredFields());
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Author> cr = cb.createQuery(Author.class);
        Root<Author> bookRoot = cr.from(Author.class);
        List<Predicate> predicates = new ArrayList<>();
        if(author!=null)
            predicates.add(cb.like(bookRoot.get("name"), "%" + author + "%"));

        List<Order> sort = sortUtil.getSort(pageable.getSort(), bookRoot, cb);

        cr.where(cb.and(predicates.toArray(new Predicate[0])));
        cr.orderBy(sort);

        CriteriaQuery<Author> select = cr.multiselect(
                bookRoot.get("id"),
                bookRoot.get("name")
        );
        TypedQuery<Author> typedQuery = entityManager.createQuery(select);
        typedQuery.setFirstResult((pageable.getPageNumber() - 1) * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());



        List<Author> results = typedQuery.getResultList();

        if (results.isEmpty()) {
            throw new NotFoundException("Нет книг или не существует страница", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(results);
    }


}
