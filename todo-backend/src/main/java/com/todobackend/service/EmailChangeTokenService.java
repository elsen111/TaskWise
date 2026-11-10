package com.todobackend.service;

public interface EmailChangeTokenService {

    String generateToken();

    String hashToken(String token);

}
