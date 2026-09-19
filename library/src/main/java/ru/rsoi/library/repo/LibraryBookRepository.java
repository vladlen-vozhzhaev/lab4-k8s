package ru.rsoi.library.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsoi.library.entity.LibraryBook;
import java.util.List;
import java.util.Optional;

public interface LibraryBookRepository extends JpaRepository<LibraryBook, Long> {
    Optional<LibraryBook> findByLibraryIdAndBookId(Long libraryId, Long bookId);
    List<LibraryBook> findByLibraryId(Long libraryId);
}