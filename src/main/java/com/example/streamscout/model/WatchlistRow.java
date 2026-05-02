package com.example.streamscout.model;

import java.time.Instant;

public record WatchlistRow(
    long id,
    long movieId,
    String title,
    String contentType,
    int releaseYear,
    String status,
    Instant createdAt
) {}

