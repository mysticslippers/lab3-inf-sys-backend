package me.ifmo.backend.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import me.ifmo.backend.config.PasswordResetProperties;
import me.ifmo.backend.entities.PasswordResetToken;
import me.ifmo.backend.entities.User;
import me.ifmo.backend.repositories.PasswordResetTokenRepository;
import me.ifmo.backend.repositories.UserRepository;
import me.ifmo.backend.services.PasswordResetService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetProperties props;

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();

        String rawToken = generateToken();
        String tokenHash = sha256Hex(rawToken);

        PasswordResetToken prt = new PasswordResetToken();
        prt.setUser(user);
        prt.setTokenHash(tokenHash);
        prt.setExpiresAt(Instant.now().plus(props.getTokenTtlMinutes(), ChronoUnit.MINUTES));
        tokenRepository.save(prt);

        SimpleMailMessage msg = getSimpleMailMessage(rawToken, user);

        mailSender.send(msg);
    }

    private SimpleMailMessage getSimpleMailMessage(String rawToken, User user) {
        String resetUrl = String.format(props.getFrontendResetUrlTemplate(), rawToken);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(user.getEmail());

        String from = props.getFromEmail();
        if (from != null && !from.isBlank()) {
            msg.setFrom(from);
        }

        msg.setSubject("Password reset");
        msg.setText("""
                Someone requested a password reset for your account.

                If it was you, open the link:
                %s

                If you didn’t request it — ignore this email.
                """.formatted(resetUrl));
        return msg;
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        String tokenHash = sha256Hex(token);

        PasswordResetToken prt = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new EntityNotFoundException("Invalid password reset token"));

        if (prt.isUsed()) {
            throw new IllegalStateException("Password reset token already used");
        }
        if (prt.isExpired()) {
            throw new IllegalStateException("Password reset token expired");
        }

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        prt.setUsedAt(Instant.now());
        tokenRepository.save(prt);
    }

    private static String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash token", e);
        }
    }
}
