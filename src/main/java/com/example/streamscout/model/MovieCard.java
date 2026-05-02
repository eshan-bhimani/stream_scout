package com.example.streamscout.model;

public record MovieCard(
    long id,
    String contentId,
    String title,
    String contentType,
    String genre,
    int releaseYear,
    Integer durationMinutes,
    Double rating,
    Double trendingScore,
    String posterUrl
) {}

