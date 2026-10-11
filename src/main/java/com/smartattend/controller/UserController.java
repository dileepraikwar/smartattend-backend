package com.smartattend.controller;

import com.smartattend.dto.FaceEnrollRequest;
import com.smartattend.model.User;
import com.smartattend.repository.UserRepository;
import com.smartattend.service.MLServiceClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final MLServiceClient mlServiceClient;

    public UserController(UserRepository userRepository, MLServiceClient mlServiceClient) {
        this.userRepository = userRepository;
        this.mlServiceClient = mlServiceClient;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/enroll-face")
    public Map<String, Object> enrollFace(@RequestBody FaceEnrollRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        int enrolledCount = 0;
        for (String imageBase64 : request.getImagesBase64()) {
            Map<String, Object> result = mlServiceClient.enrollFace(user.getId(), imageBase64);
            if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                enrolledCount++;
            }
        }

        return Map.of(
                "success", enrolledCount > 0,
                "enrolledPhotos", enrolledCount,
                "message", "Face enrolled for " + user.getName()
        );
    }
}
