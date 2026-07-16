package com.smartroad.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.smartroad.backend.service.GeminiService;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:5173")
public class AIController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/severity")
    public String predictSeverity(
            @RequestBody Map<String,String> body){

        return geminiService.predictSeverity(
                body.get("description"));

    }

}