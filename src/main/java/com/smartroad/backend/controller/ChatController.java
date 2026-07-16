package com.smartroad.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.smartroad.backend.service.GeminiService;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping
    public String chat(@RequestBody Map<String,String> body){

        return geminiService.askGemini(body.get("message"));

    }

}