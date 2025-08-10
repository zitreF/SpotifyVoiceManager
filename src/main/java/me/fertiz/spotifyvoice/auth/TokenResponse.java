package me.fertiz.spotifyvoice.auth;

public record TokenResponse(String accessToken, String refreshToken, int expiresIn) {}
