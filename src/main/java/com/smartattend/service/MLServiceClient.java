package com.smartattend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class MLServiceClient {

    @Value("${ml.service.url}")
    private String mlServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public Map<String, Object> enrollFace(Long userId, String imageBase64) {
        Map<String, Object> body = Map.of("user_id", userId, "image_base64", imageBase64);
        return restTemplate.postForObject(mlServiceUrl + "/enroll", body, Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyFace(Long userId, String imageBase64) {
        Map<String, Object> body = Map.of("user_id", userId, "image_base64", imageBase64);
        return restTemplate.postForObject(mlServiceUrl + "/verify", body, Map.class);
    }
}
