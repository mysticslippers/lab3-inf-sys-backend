package me.ifmo.backend.services;

public interface PasswordResetService {

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);
}
