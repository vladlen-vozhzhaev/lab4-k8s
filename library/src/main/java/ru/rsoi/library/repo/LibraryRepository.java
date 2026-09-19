package ru.rsoi.library.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsoi.library.entity.Library;
import java.util.Optional;
import java.util.UUID;

public interface LibraryRepository extends JpaRepository<Library, Long> {
    Page<Library> findByCity(String city, Pageable pageable);
    Optional<Library> findByLibraryUid(UUID libraryUid);
}