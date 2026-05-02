package com.example.streamscout.model;

public record AppUser(
    long id,
    String username,
    String passwordHash,
    String role,
    boolean enabled
) {}

