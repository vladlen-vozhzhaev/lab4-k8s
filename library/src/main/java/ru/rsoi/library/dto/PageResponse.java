package ru.rsoi.library.dto;

import java.util.List;

public record PageResponse<T>(int page, int pageSize, long totalElements, List<T> items) {}