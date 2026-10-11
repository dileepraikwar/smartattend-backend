package com.smartattend.controller;

import com.smartattend.dto.AuthResponse;
import com.smartattend.dto.LoginRequest;
import com.smartattend.dto.OtpVerifyRequest;
import com.smartattend.dto.RegisterRequest;
import com.smartattend.model.User;
import com.smartattend.service.AuthService;
import com.smartattend.service.EmailService;
import com.smartattend.service.OtpService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final EmailService emailService;

    public AuthController(AuthService authService, OtpService otpService, EmailService emailService) {
        this.authService = authService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/register/request-otp")
    public Map<String, String> requestOtp(@RequestBody RegisterRequest request) {
        String otp = otpService.generateAndStoreOtp(request);
        emailService.sendOtpEmail(request.getEmail(), otp);
        return Map.of("message", "OTP sent to " + request.getEmail());
    }

    @PostMapping("/register/verify-otp")
    public User verifyOtpAndRegister(@RequestBody OtpVerifyRequest request) {
        RegisterRequest pending = otpService.verifyOtp(request.getEmail(), request.getOtp());
        return authService.register(pending);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}