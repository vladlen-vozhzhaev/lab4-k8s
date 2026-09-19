package ru.rsoi.library.dto;

import java.util.UUID;

public record LibraryDto(UUID libraryUid, String name, String address, String city) {}