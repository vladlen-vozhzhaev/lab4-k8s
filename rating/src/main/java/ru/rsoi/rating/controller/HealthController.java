package ru.rsoi.rating.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manage")
public class HealthController {
    @GetMapping("/health")
    public ResponseEntity<Void> health() { return ResponseEntity.ok().build(); }
}