package ru.rsoi.reservation.dto;

import java.time.LocalDate;

public record ReturnRequest(String condition, LocalDate date) {}