package com.example.streamscout.model;

public record PlatformStats(
    long platformId,
    String platformName,
    long titleCount,
    double avgRating,
    double avgTrending
) {}

