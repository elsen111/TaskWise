package com.todobackend.service;

public interface EmailService {

    void sendPasswordResetOtp(String recipient, String otp);

    void sendEmailChangeVerification(String recipient, String rawToken);

    void sendEmailChangeNotification(String oldEmail, String newEmail);

}
