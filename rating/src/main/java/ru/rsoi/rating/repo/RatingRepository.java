package ru.rsoi.rating.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsoi.rating.entity.Rating;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUsername(String username);
}