package ru.rsoi.rating;

import org.junit.jupiter.api.Test;
import ru.rsoi.rating.entity.Rating;
import ru.rsoi.rating.repo.RatingRepository;
import ru.rsoi.rating.service.RatingService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RatingServiceTest {

    @Test
    void clampsTo100() {
        RatingRepository repo = mock(RatingRepository.class);
        Rating r = new Rating(); r.setUsername("u"); r.setStars(95);
        when(repo.findByUsername("u")).thenReturn(Optional.of(r));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RatingService s = new RatingService(repo);
        assertEquals(100, s.change("u", 50).getStars());
    }

    @Test
    void clampsTo1() {
        RatingRepository repo = mock(RatingRepository.class);
        Rating r = new Rating(); r.setUsername("u"); r.setStars(5);
        when(repo.findByUsername("u")).thenReturn(Optional.of(r));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RatingService s = new RatingService(repo);
        assertEquals(1, s.change("u", -50).getStars());
    }
}