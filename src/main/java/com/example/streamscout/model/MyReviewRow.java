package com.example.streamscout.model;

import java.time.Instant;

public record MyReviewRow(
    long id,
    long movieId,
    String movieTitle,
    int stars,
    String reviewText,
    Instant createdAt
) {}
