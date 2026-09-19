package ru.rsoi.reservation.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name = "reservation")
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "reservation_uid", unique = true, nullable = false)
    private UUID reservationUid;
    @Column(nullable = false, length = 80)
    private String username;
    @Column(name = "book_uid", nullable = false)
    private UUID bookUid;
    @Column(name = "library_uid", nullable = false)
    private UUID libraryUid;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "till_date", nullable = false)
    private LocalDate tillDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getReservationUid() { return reservationUid; }
    public void setReservationUid(UUID reservationUid) { this.reservationUid = reservationUid; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public UUID getBookUid() { return bookUid; }
    public void setBookUid(UUID bookUid) { this.bookUid = bookUid; }
    public UUID getLibraryUid() { return libraryUid; }
    public void setLibraryUid(UUID libraryUid) { this.libraryUid = libraryUid; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getTillDate() { return tillDate; }
    public void setTillDate(LocalDate tillDate) { this.tillDate = tillDate; }
}