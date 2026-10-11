package com.smartattend.service;

import com.smartattend.dto.RegisterRequest;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static class OtpEntry {
        String otp;
        RegisterRequest pendingRequest;
        long expiryTime;

        OtpEntry(String otp, RegisterRequest pendingRequest, long expiryTime) {
            this.otp = otp;
            this.pendingRequest = pendingRequest;
            this.expiryTime = expiryTime;
        }
    }

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private static final long OTP_VALID_MS = 5 * 60 * 1000;

    public String generateAndStoreOtp(RegisterRequest request) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        long expiry = System.currentTimeMillis() + OTP_VALID_MS;
        otpStore.put(request.getEmail(), new OtpEntry(otp, request, expiry));
        return otp;
    }

    public RegisterRequest verifyOtp(String email, String otp) {
        OtpEntry entry = otpStore.get(email);
        if (entry == null) {
            throw new RuntimeException("No OTP request found for this email");
        }
        if (System.currentTimeMillis() > entry.expiryTime) {
            otpStore.remove(email);
            throw new RuntimeException("OTP expired, please request a new one");
        }
        if (!entry.otp.equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
        otpStore.remove(email);
        return entry.pendingRequest;
    }
}