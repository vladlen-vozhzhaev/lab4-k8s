package ru.rsoi.reservation.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rsoi.reservation.entity.Reservation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUsernameOrderByIdDesc(String username);
    Optional<Reservation> findByReservationUid(UUID reservationUid);
    long countByUsernameAndStatus(String username, String status);
}