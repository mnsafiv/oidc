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
import ru.safonoviv.oidclmsboi.lms.entities.*;
import ru.safonoviv.oidclmsboi.lms.exceptions.ExceptionCustomRollback;
import ru.safonoviv.oidclmsboi.lms.exceptions.NotFoundException;
import ru.safonoviv.oidclmsboi.lms.repository.RegisterRepository;
import ru.safonoviv.oidclmsboi.lms.service.RegisterService;
import ru.safonoviv.oidclmsboi.lms.service.UserService;
import ru.safonoviv.oidclmsboi.lms.util.SortUtil;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private final RegisterRepository registerRepo;
    private final UserService userService;
    private final SortUtil sortUtil;

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    public ResponseEntity<?> registerReservationBook(RegisterBookReserve registerBookReserve, OAuth2AuthenticationToken token) throws ExceptionCustomRollback {
        User user = userService.findByUsername(token.getName());
        if (user == null) {
            throw new NotFoundException(String.format("User not found: %s", token.getName()), HttpStatus.BAD_REQUEST);
        }
        registerBookReserve.setUser(user);
        registerBookReserve.setTimeCreated(new java.util.Date());
        registerBookReserve.setValid(true);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<Book> cu = cb.createCriteriaUpdate(Book.class);
        Root<Book> root = cu.from(Book.class);
        Predicate bookId = cb.equal(root.get("id"), registerBookReserve.getBook().getId());
        Predicate isAvailable = cb.equal(root.get("isAvailable"), true);
        cu.set("isAvailable", false);
        cu.where(bookId, isAvailable);
        int updateCount = entityManager.createQuery(cu).executeUpdate();
        if (updateCount == 1) {
            registerRepo.save(registerBookReserve);
            return ResponseEntity.ok(registerBookReserve);
        }

        throw new ExceptionCustomRollback("Не удалось создать", HttpStatus.BAD_REQUEST);
    }


    @Override
    @Transactional
    public ResponseEntity<?> myAvailableBook(OAuth2AuthenticationToken token) {
        User user = userService.findByUsername(token.getName());
        if (user == null) {
            throw new NotFoundException(String.format("User not found: %s", token.getName()), HttpStatus.BAD_REQUEST);
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RegisterBookReserve> cr = cb.createQuery(RegisterBookReserve.class);
        Root<RegisterBookReserve> root = cr.from(RegisterBookReserve.class);


        Predicate userId = cb.equal(root.get("userId").get("id"), user.getId());
        Predicate timeNext = cb.greaterThanOrEqualTo(root.get("dateTakeBook"), LocalDate.now());
        Predicate timePast = cb.lessThanOrEqualTo(root.get("dataReturnBook"), LocalDate.now());


        cr.where(userId, timeNext, timePast);
        cr.multiselect(root);

        List<RegisterBookReserve> results = entityManager.createQuery(cr).getResultList();


        return ResponseEntity.ok("fdd");
    }


    @Override
    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    public ResponseEntity<?> registerReturnBook(long registerId) throws ExceptionCustomRollback {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaUpdate<RegisterBookConfirm> criteriaUpdateConfirm = cb.createCriteriaUpdate(RegisterBookConfirm.class);
        Root<RegisterBookConfirm> root = criteriaUpdateConfirm.from(RegisterBookConfirm.class);
        Subquery<Long> sub = criteriaUpdateConfirm.subquery(Long.class);
        Root<RegisterBookReserve> subRoot = sub.from(RegisterBookReserve.class);
        sub.select(subRoot.get("id"))
                .where(cb.equal(subRoot.get("id"), registerId));
        Predicate predicateValid = cb.equal(root.get("valid"), true);
        criteriaUpdateConfirm.set("valid", false);
        criteriaUpdateConfirm.where(predicateValid, cb.in(root.get("id")).value(sub));

        CriteriaUpdate<Book> criteriaUpdateBook = cb.createCriteriaUpdate(Book.class);
        Root<Book> bookRoot = criteriaUpdateBook.from(Book.class);
        Predicate bookAvailable = cb.equal(bookRoot.get("isAvailable"), false);
        Subquery<Long> sub2 = criteriaUpdateBook.subquery(Long.class);
        Root<RegisterBookReserve> subRoot2 = sub2.from(RegisterBookReserve.class);
        sub2.select(subRoot2.get("book").get("id"))
                .where(cb.equal(subRoot2.get("id"), registerId));
        criteriaUpdateBook.set("isAvailable", true);
        criteriaUpdateBook.where(bookAvailable, cb.in(bookRoot.get("id")).value(sub2));


        if (entityManager.createQuery(criteriaUpdateConfirm).executeUpdate() == 1 && entityManager.createQuery(criteriaUpdateBook).executeUpdate() == 1) {
            RegisterBookReturn registerBookReturn = RegisterBookReturn.builder()
                    .bookReserve(new RegisterBookReserve(registerId))
                    .timeCreated(new java.util.Date())
                    .valid(true)
                    .build();

            entityManager.persist(registerBookReturn);
            return ResponseEntity.ok("Updated!");

        }


        throw new ExceptionCustomRollback("Не удалось обновить", HttpStatus.BAD_REQUEST);
    }


    @Override
    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    public ResponseEntity<?> registerConfirmBook(long id, OAuth2AuthenticationToken token) throws ExceptionCustomRollback {
        User user = userService.findByUsername(token.getName());
        if (user == null) {
            throw new NotFoundException(String.format("User not found: %s", token.getName()), HttpStatus.BAD_REQUEST);
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<RegisterBookReserve> cu = cb.createCriteriaUpdate(RegisterBookReserve.class);
        Root<RegisterBookReserve> root = cu.from(RegisterBookReserve.class);

        Predicate predicateBookId = cb.equal(root.get("id"), id);
        Predicate predicateUserId = cb.equal(root.get("user").get("id"), user.getId());
        Predicate predicateValid = cb.equal(root.get("valid"), true);
        Predicate timeStart = cb.lessThanOrEqualTo(root.get("dateTakeBookStart").as(Date.class), Date.valueOf(LocalDate.now()));
        Predicate timeEnd = cb.greaterThanOrEqualTo(root.get("dateTakeBookEnd").as(Date.class), Date.valueOf(LocalDate.now()));

        cu.set("valid", false);

        cu.where(predicateBookId, predicateUserId, predicateValid, timeStart, timeEnd);

        if (entityManager.createQuery(cu).executeUpdate() == 1) {
            RegisterBookConfirm registerBookConfirm = RegisterBookConfirm.builder()
                    .bookReserve(new RegisterBookReserve(id))
                    .timeCreated(new java.util.Date())
                    .valid(true)
                    .build();
            entityManager.persist(registerBookConfirm);

            return ResponseEntity.ok("Обновлено!");
        }

        throw new ExceptionCustomRollback("Не удалось обновить", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<?> availableBook(String search, boolean available, Pageable pageable, OAuth2AuthenticationToken token) throws NotFoundException {
        User user = userService.findByUsername(token.getName());
        if (user == null) {
            throw new NotFoundException(String.format("User not found: %s", token.getName()), HttpStatus.BAD_REQUEST);
        }
        if (pageable.getPageNumber() < 1) {
            throw new NotFoundException("Bad request!", HttpStatus.BAD_REQUEST);
        }
        sortUtil.checkSortParameters(pageable.getSort(), Book.class.getDeclaredFields());

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RegisterBookReserve> criteriaSelectRegister = cb.createQuery(RegisterBookReserve.class);
        Root<RegisterBookReserve> root = criteriaSelectRegister.from(RegisterBookReserve.class);
        List<Order> sort = sortUtil.getSort(pageable.getSort(), root, cb);

        Predicate predicateUser = cb.equal(root.get("user").get("id"), user.getId());
        Predicate predicateValid = cb.equal(root.get("valid"), true);
        Predicate timeStart = cb.lessThanOrEqualTo(root.get("dateTakeBookStart").as(Date.class), Date.valueOf(LocalDate.now()));
        Predicate timeEnd = cb.greaterThanOrEqualTo(root.get("dateTakeBookEnd").as(Date.class), Date.valueOf(LocalDate.now()));
        criteriaSelectRegister.where(predicateUser, predicateValid, timeStart, timeEnd);

        criteriaSelectRegister.orderBy(sort);

        CriteriaQuery<RegisterBookReserve> select = criteriaSelectRegister.multiselect(
                root.get("id"),
                root.get("book"),
                root.get("user"),
                root.get("dateTakeBookStart"),
                root.get("dateTakeBookEnd"),
                root.get("timeCreated"),
                root.get("valid")
        );

        TypedQuery<RegisterBookReserve> typedQuery = entityManager.createQuery(select);

        typedQuery.setFirstResult((pageable.getPageNumber() - 1) * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<RegisterBookReserve> results = typedQuery.getResultList();
        if (results.isEmpty()) {
            throw new NotFoundException("Not found", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(results);
    }

    @Override
    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    public ResponseEntity<?> registerClosedBook(Long registerReserveId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaUpdate<RegisterBookReserve> criteriaUpdateReserve = cb.createCriteriaUpdate(RegisterBookReserve.class);
        Root<RegisterBookReserve> root = criteriaUpdateReserve.from(RegisterBookReserve.class);
        Predicate predicateValid = cb.equal(root.get("valid"), true);
        Predicate predicateId = cb.equal(root.get("id"), registerReserveId);
        criteriaUpdateReserve.set("valid", false);
        criteriaUpdateReserve.where(predicateValid, predicateId);

        CriteriaUpdate<Book> criteriaUpdateBook = cb.createCriteriaUpdate(Book.class);
        Root<Book> bookRoot = criteriaUpdateBook.from(Book.class);
        Predicate bookAvailable = cb.equal(bookRoot.get("isAvailable"), false);
        Subquery<Long> sub2 = criteriaUpdateBook.subquery(Long.class);
        Root<RegisterBookReserve> subRoot2 = sub2.from(RegisterBookReserve.class);
        sub2.select(subRoot2.get("book").get("id"))
                .where(cb.equal(subRoot2.get("id"), registerReserveId));
        criteriaUpdateBook.set("isAvailable", true);
        criteriaUpdateBook.where(bookAvailable, cb.in(bookRoot.get("id")).value(sub2));

        if (entityManager.createQuery(criteriaUpdateReserve).executeUpdate() == 1 && entityManager.createQuery(criteriaUpdateBook).executeUpdate() == 1) {
            RegisterBookReturn registerBookReturn = RegisterBookReturn.builder()
                    .bookReserve(new RegisterBookReserve(registerReserveId))
                    .timeCreated(new java.util.Date())
                    .valid(true)
                    .build();

            entityManager.persist(registerBookReturn);
            return ResponseEntity.ok("Updated!");
        }
        throw new ExceptionCustomRollback("Не удалось обновить", HttpStatus.BAD_REQUEST);
    }

    @Override
    @Transactional(rollbackOn = ExceptionCustomRollback.class)
    public ResponseEntity<?> registerFeedback(BookFeedback feedback, OAuth2AuthenticationToken token) throws ExceptionCustomRollback {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaUpdate<RegisterBookReturn> cu = cb.createCriteriaUpdate(RegisterBookReturn.class);
        Root<RegisterBookReturn> root = cu.from(RegisterBookReturn.class);
        Predicate predicateValid = cb.equal(root.get("valid"), true);
        Predicate predicateId = cb.equal(root.get("bookReserve").get("id"), feedback.getBookReserve().getId());
        cu.set("valid", false);
        cu.where(predicateValid, predicateId);
        if (entityManager.createQuery(cu).executeUpdate() == 1) {
            entityManager.persist(feedback);
            if (feedback.getId() != 0) {
                return ResponseEntity.ok("Feedback saved");
            }
        }

        throw new ExceptionCustomRollback("Не удалось оставить отзыв", HttpStatus.BAD_REQUEST);
    }
}
