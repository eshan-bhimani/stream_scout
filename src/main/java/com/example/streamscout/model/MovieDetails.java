package com.example.streamscout.model;

import java.util.List;

public record MovieDetails(
    long id,
    String contentId,
    String title,
    String contentType,
    String genre,
    String country,
    String language,
    int releaseYear,
    int durationMinutes,
    Double rating,
    Integer votes,
    Double weightedRating,
    Double engagementScore,
    Double popularityScore,
    Double trendingScore,
    String tags,
    String description,
    String posterUrl,
    List<String> platforms,
    Double avgStars,
    Integer reviewCount
) {}

