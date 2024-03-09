package ru.safonoviv.oidclmsboi.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.safonoviv.oidclmsboi.lms.entities.Author;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
