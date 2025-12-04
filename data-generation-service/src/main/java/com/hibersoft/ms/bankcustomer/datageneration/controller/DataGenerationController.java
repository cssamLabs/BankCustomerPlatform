package com.hibersoft.ms.bankcustomer.datageneration.controller;

import com.hibersoft.ms.bankcustomer.datageneration.service.DataGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/generate")
public class DataGenerationController {

    @Autowired
    private DataGenerationService generationService;

    @PostMapping("/bank/{bankId}")
    public ResponseEntity<Map<String, String>> generateDataForBank(
            @PathVariable String bankId,
            @RequestParam(defaultValue = "100") int count) {
        
        int generatedCount = generationService.generateData(bankId, count);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "bankId", bankId,
            "recordsGenerated", String.valueOf(generatedCount),
            "message", "Data generation started successfully."
        ));
    }
}
