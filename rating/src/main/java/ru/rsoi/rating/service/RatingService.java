package ru.rsoi.rating.service;

import org.springframework.stereotype.Service;
import ru.rsoi.rating.entity.Rating;
import ru.rsoi.rating.repo.RatingRepository;

@Service
public class RatingService {
    private final RatingRepository repo;
    public RatingService(RatingRepository repo) { this.repo = repo; }

    public Rating getOrCreate(String username) {
        return repo.findByUsername(username).orElseGet(() -> {
            Rating r = new Rating();
            r.setUsername(username);
            r.setStars(1);
            return repo.save(r);
        });
    }

    public Rating change(String username, int delta) {
        Rating r = getOrCreate(username);
        int next = Math.max(1, Math.min(100, r.getStars() + delta));
        r.setStars(next);
        return repo.save(r);
    }
}