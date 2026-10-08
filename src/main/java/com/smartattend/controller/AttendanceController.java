package com.smartattend.controller;

import com.smartattend.dto.CheckInRequest;
import com.smartattend.model.AttendanceLog;
import com.smartattend.model.User;
import com.smartattend.repository.AttendanceLogRepository;
import com.smartattend.repository.UserRepository;
import com.smartattend.service.MLServiceClient;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final UserRepository userRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final MLServiceClient mlServiceClient;
    private final SimpMessagingTemplate messagingTemplate;

    public AttendanceController(UserRepository userRepository,
                                 AttendanceLogRepository attendanceLogRepository,
                                 MLServiceClient mlServiceClient,
                                 SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.attendanceLogRepository = attendanceLogRepository;
        this.mlServiceClient = mlServiceClient;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/checkin")
    public Map<String, Object> checkIn(@RequestBody CheckInRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate today = LocalDate.now();
        Optional<AttendanceLog> todayLog = attendanceLogRepository
                .findTopByUserIdAndCheckInTimeBetweenOrderByCheckInTimeDesc(
                        user.getId(), today.atStartOfDay(), today.atTime(LocalTime.MAX));

        if (todayLog.isPresent()) {
            if (todayLog.get().getCheckOutTime() == null) {
                return Map.of("success", false, "message", "Already checked in — please check out first");
            }
            return Map.of("success", false, "message", "Attendance already marked for today");
        }

        Map<String, Object> verifyResult = mlServiceClient.verifyFace(user.getId(), request.getImageBase64());
        boolean match = verifyResult != null && Boolean.TRUE.equals(verifyResult.get("match"));
        double confidence = (verifyResult != null && verifyResult.get("confidence") != null)
                ? ((Number) verifyResult.get("confidence")).doubleValue() : 0.0;

        if (!match) {
            return Map.of("success", false, "message", "Face not recognized");
        }

        AttendanceLog log = new AttendanceLog();
        log.setUser(user);
        log.setCheckInTime(LocalDateTime.now());
        log.setVerificationConfidence((float) confidence);
        log.setStatus("PRESENT");
        attendanceLogRepository.save(log);

        messagingTemplate.convertAndSend("/topic/attendance",
                Map.of("user", user.getName(), "time", log.getCheckInTime().toString()));

        return Map.of("success", true, "message", "Checked in", "confidence", confidence);
    }

    @PostMapping("/checkout")
    public Map<String, Object> checkOut(@RequestBody CheckInRequest request) {
        AttendanceLog log = attendanceLogRepository
                .findTopByUserIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(request.getUserId())
                .orElse(null);

        if (log == null) {
            return Map.of("success", false, "message", "You are not checked in");
        }

        log.setCheckOutTime(LocalDateTime.now());
        attendanceLogRepository.save(log);

        return Map.of("success", true, "message", "Checked out");
    }

    @GetMapping("/report")
    public List<AttendanceLog> report(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return attendanceLogRepository.findByUserId(userId);
        }
        return attendanceLogRepository.findAll();
    }
}