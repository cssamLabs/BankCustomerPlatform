package com.hibersoft.ms.bankcustomer.simpledatamodeling.service;

import com.hibersoft.ms.bankcustomer.simpledatamodeling.model.CustomerProfile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AimlServiceCaller {

    // Use the Docker internal service name and port here
    private final String segmentationServiceUrl = "http://customer-segmentation-app:5000/api/v1/segmentation/predict-segment";
    private final RestTemplate restTemplate = new RestTemplate();

    public Integer getCustomerSegment(Map<String, BigDecimal> spendingProfile) {
        // Convert the BigDecimal map to the string-based format the Python API expects
        Map<String, String> stringProfile = new HashMap<>();
        spendingProfile.forEach((category, amount) -> stringProfile.put(category, amount.toPlainString()));

        // The Python API expects a list of profiles
        List<Map<String, String>> requestBody = Collections.singletonList(stringProfile);

        // Make the POST request
        // The response structure is {"predictions": [segment_id]}
        Map<String, List<Integer>> response = restTemplate.postForObject(
            segmentationServiceUrl, 
            requestBody, 
            Map.class
        );

        if (response != null && response.containsKey("predictions") && !response.get("predictions").isEmpty()) {
            return response.get("predictions").get(0);
        }
        return null; // Return null if prediction fails
    }
}

// You will also need a simple DTO to match the structure expected by the Python API
// package com.hibersoft.ms.bankcustomer.simpledatamodeling.model;
// public class CustomerProfile { ... fields Utilities, Groceries as String ... }
