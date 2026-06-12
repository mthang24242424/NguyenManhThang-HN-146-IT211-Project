package com.example.project.service;

public interface TokenBlacklistService {

    void revoke(String token, long ttlMillis);

    boolean isRevoked(String token);
}
