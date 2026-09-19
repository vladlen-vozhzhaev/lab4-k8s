package ru.rsoi.library.dto;

import java.util.UUID;

public record BookDto(UUID bookUid, String name, String author, String genre,
                      String condition, Integer availableCount) {}