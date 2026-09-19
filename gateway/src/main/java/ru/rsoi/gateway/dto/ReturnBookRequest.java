package ru.rsoi.gateway.dto;

import java.time.LocalDate;

public record ReturnBookRequest(String condition, LocalDate date) {}