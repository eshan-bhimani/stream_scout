package com.example.streamscout.model;

import java.time.Instant;

public record ReviewRow(
    long id,
    String username,
    int stars,
    String reviewText,
    Instant createdAt
) {}

