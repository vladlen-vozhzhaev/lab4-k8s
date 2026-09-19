package ru.rsoi.library.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsoi.library.entity.Book;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByBookUid(UUID bookUid);
}