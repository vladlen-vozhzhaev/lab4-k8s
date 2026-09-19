package ru.rsoi.rating.controller;

import org.springframework.web.bind.annotation.*;
import ru.rsoi.rating.service.RatingService;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rating")
public class RatingController {
    private final RatingService service;
    public RatingController(RatingService service) { this.service = service; }

    @GetMapping
    public Map<String, Integer> get(@RequestHeader("X-User-Name") String username) {
        return Map.of("stars", service.getOrCreate(username).getStars());
    }

    @PostMapping
    public Map<String, Integer> change(@RequestHeader("X-User-Name") String username,
                                       @RequestParam("delta") int delta) {
        return Map.of("stars", service.change(username, delta).getStars());
    }
}