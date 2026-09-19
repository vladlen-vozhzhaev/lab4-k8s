package ru.rsoi.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rsoi.reservation.dto.CreateReservationRequest;
import ru.rsoi.reservation.dto.ReturnRequest;
import ru.rsoi.reservation.entity.Reservation;
import ru.rsoi.reservation.repo.ReservationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationRepository repo;

    public ReservationController(ReservationRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Reservation> list(@RequestHeader("X-User-Name") String username) {
        return repo.findByUsernameOrderByIdDesc(username);
    }

    @GetMapping("/count-active")
    public long countActive(@RequestHeader("X-User-Name") String username) {
        return repo.countByUsernameAndStatus(username, "RENTED");
    }

    @PostMapping
    public Reservation create(@RequestHeader("X-User-Name") String username,
                              @RequestBody CreateReservationRequest req) {
        Reservation r = new Reservation();
        r.setReservationUid(UUID.randomUUID());
        r.setUsername(username);
        r.setBookUid(req.bookUid());
        r.setLibraryUid(req.libraryUid());
        r.setStatus("RENTED");
        r.setStartDate(LocalDate.now());
        r.setTillDate(req.tillDate());
        return repo.save(r);
    }

    @PostMapping("/{reservationUid}/return")
    public ResponseEntity<Reservation> returnBook(@RequestHeader("X-User-Name") String username,
                                                  @PathVariable UUID reservationUid,
                                                  @RequestBody ReturnRequest req) {
        var rOpt = repo.findByReservationUid(reservationUid);
        if (rOpt.isEmpty()) return ResponseEntity.notFound().build();
        Reservation r = rOpt.get();
        if (!r.getUsername().equals(username)) return ResponseEntity.notFound().build();
        if (req.date() != null && req.date().isAfter(r.getTillDate())) {
            r.setStatus("EXPIRED");
        } else {
            r.setStatus("RETURNED");
        }
        return ResponseEntity.ok(repo.save(r));
    }
}