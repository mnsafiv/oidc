package ru.safonoviv.oidclmsboi.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.safonoviv.oidclmsboi.lms.entities.BookFeedback;

@Repository
public interface BookFeedbackRepository extends JpaRepository<BookFeedback, Long> {
}
