package ru.rsoi.reservation.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateReservationRequest(UUID bookUid, UUID libraryUid, LocalDate tillDate) {}