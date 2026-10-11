package com.smartattend.repository;

import com.smartattend.model.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {
    List<AttendanceLog> findByUserId(Long userId);
    Optional<AttendanceLog> findTopByUserIdOrderByCheckInTimeDesc(Long userId);
    Optional<AttendanceLog> findTopByUserIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(Long userId);
    Optional<AttendanceLog> findTopByUserIdAndCheckInTimeBetweenOrderByCheckInTimeDesc(
            Long userId, LocalDateTime start, LocalDateTime end);
}