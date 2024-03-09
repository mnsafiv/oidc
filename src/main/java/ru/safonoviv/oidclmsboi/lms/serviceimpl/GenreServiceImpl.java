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
import ru.safonoviv.oidclmsboi.lms.dto.GenreResponse;
import ru.safonoviv.oidclmsboi.lms.entities.*;
import ru.safonoviv.oidclmsboi.lms.exceptions.ExceptionCustomRollback;
import ru.safonoviv.oidclmsboi.lms.exceptions.NotFoundException;
import ru.safonoviv.oidclmsboi.lms.repository.GenreRepository;
import ru.safonoviv.oidclmsboi.lms.service.GenreService;
import ru.safonoviv.oidclmsboi.lms.util.SortUtil;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    @PersistenceContext
    private EntityManager entityManager;
    private final GenreRepository genreRepo;
    private final SortUtil sortUtil;


    @Override
    @Transactional
    public ResponseEntity<?> saveGenre(Genre genre, OAuth2AuthenticationToken token) throws ExceptionCustomRollback {
        if (token.getAuthorities().stream().anyMatch(t -> t.getAuthority().equals("ROLE_ADMIN"))) {
            genre = genreRepo.save(genre);
            if (genre.getId() != null) {
                return ResponseEntity.ok(genre.getId() + " " + genre.getName());
            }
        }

        throw new ExceptionCustomRollback("Не удалось сохранить", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> updateGenre(Long id, Genre genre, OAuth2AuthenticationToken token) throws ExceptionCustomRollback {
        if (token.getAuthorities().stream().anyMatch(t -> t.getAuthority().equals("ROLE_ADMIN"))) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaUpdate<Genre> criteriaUpdateBook = cb.createCriteriaUpdate(Genre.class);
            Root<Genre> bookRootUpdate = criteriaUpdateBook.from(Genre.class);
            criteriaUpdateBook.where(cb.equal(bookRootUpdate.get("id"), id));
            criteriaUpdateBook.set("name", genre.getName());
            if (entityManager.createQuery(criteriaUpdateBook).executeUpdate() == 1) {
                return ResponseEntity.ok("Успешно сохранено");
            }
        }
        throw new NotFoundException("Не удалось обновить", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> findGenre(String genre, Pageable pageable) throws NotFoundException {
        if (pageable.getPageNumber() < 1) {
            throw new NotFoundException("Bad request!", HttpStatus.BAD_REQUEST);
        }
        sortUtil.checkSortParameters(pageable.getSort(), Book.class.getDeclaredFields());
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<GenreResponse> cr = cb.createQuery(GenreResponse.class);
        Root<Genre> root = cr.from(Genre.class);
        List<Order> sort = sortUtil.getSort(pageable.getSort(), root, cb);
        List<Predicate> predicates = new ArrayList<>();
        if (genre != null) {
            predicates.add(cb.equal(root.get("name"), "%" + genre + "%"));
        }


        cr.where(cb.and(predicates.toArray(new Predicate[0])));
        cr.orderBy(sort);

        CriteriaQuery<GenreResponse> select = cr.multiselect(
                root.get("id"),
                root.get("name"));

        TypedQuery<GenreResponse> typedQuery = entityManager.createQuery(select);
        typedQuery.setFirstResult((pageable.getPageNumber() - 1) * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<GenreResponse> results = typedQuery.getResultList();
        if (results.isEmpty()) {
            throw new NotFoundException("No genre!", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(results);
    }


}
