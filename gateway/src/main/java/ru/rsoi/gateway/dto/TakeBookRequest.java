package ru.rsoi.gateway.dto;

import java.time.LocalDate;
import java.util.UUID;

public record TakeBookRequest(UUID bookUid, UUID libraryUid, LocalDate tillDate) {}