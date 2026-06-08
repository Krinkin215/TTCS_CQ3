package com.example.demo.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.demo.dto.request.PredictRequestDTO;
import com.example.demo.dto.response.PredictResponseDTO;
import com.example.demo.service.MLService;
import org.springframework.beans.factory.annotation.Value;

@Service
public class MLServiceImpl implements MLService {
    @Value("${ml.api.url}")
    private String url;
    private final RestTemplate restTemplate = new RestTemplate(); //nối Spring Boot(Java) với Flask(python)

    @Override
    public PredictResponseDTO predict(PredictRequestDTO request) {
        try {
            PredictResponseDTO response = restTemplate.postForObject(
                url,
                request,
                PredictResponseDTO.class
            );

            System.out.println("RESPONSE: " + response);

            return response;

        } catch (Exception e) {
            System.out.println("=== ML API ERROR ===");
            e.printStackTrace();

            // fallback tránh crash hệ thống
            return PredictResponseDTO.builder()
                    .p_forget(0.5f)  // giá trị default
                    .build();
        }
    }
}
