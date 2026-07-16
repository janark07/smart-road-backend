package com.smartroad.backend.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final String URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public String askGemini(String question) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = """
    You are Smart Road AI, an intelligent Road Safety Assistant.

    You should answer ONLY questions related to:
    - Road accidents
    - First aid
    - Ambulance
    - Hospitals
    - Traffic rules
    - Road safety
    - Emergency response
    - Accident prevention

    Formatting Rules:
    - Always answer in proper Markdown.
    - Use headings (##).
    - Use numbered lists when appropriate.
    - Use bullet points.
    - Leave one blank line between sections.
    - Highlight important words using **bold**.
    - Never compress everything into one paragraph.
    - Keep answers short, professional, and easy to read.
    - End with a short safety reminder.

    User Question:

    """ + question;

        Map<String, Object> body = Map.of(
                "contents",
                List.of(
                        Map.of(
                                "parts",
                                List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        URL + apiKey,
                        HttpMethod.POST,
                        entity,
                        Map.class
                );

        try {

            List candidates =
                    (List) response.getBody().get("candidates");

            Map candidate =
                    (Map) candidates.get(0);

            Map content =
                    (Map) candidate.get("content");

            List parts =
                    (List) content.get("parts");

            Map part =
                    (Map) parts.get(0);

            String answer = part.get("text").toString();

            return answer.trim();

        } catch (Exception e) {

            return "Sorry, I couldn't generate a response.";

        }
    }
    
    
    public String predictSeverity(String description) {

        try {

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(

                    "contents",

                    List.of(

                            Map.of(

                                    "parts",

                                    List.of(

                                            Map.of(

                                            		"text",

                                            		"""
                                            		You are an intelligent Accident Analysis AI for a Smart Road Accident Reporting System.

                                            		Analyze the accident description carefully.

                                            		Return your response ONLY in the following Markdown format.

                                            		## Severity
                                            		Low / Medium / High / Critical

                                            		## Reason
                                            		- Point 1
                                            		- Point 2
                                            		- Point 3

                                            		## Recommendation
                                            		- Recommendation 1
                                            		- Recommendation 2
                                            		- Recommendation 3

                                            		Rules:
                                            		- Severity must be exactly one of:
                                            		  Low
                                            		  Medium
                                            		  High
                                            		  Critical
                                            		- Give only 2–4 short bullet points for Reason.
                                            		- Give only 2–4 short bullet points for Recommendation.
                                            		- Do NOT write long paragraphs.
                                            		- Do NOT include introductions or conclusions.
                                            		- Do NOT include any extra text before or after the response.
                                            		- Base the severity only on the given accident description.

                                            		Accident Description:

                                            		""" + description
                                            )

                                    )

                            )

                    )

            );

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            URL + apiKey,
                            HttpMethod.POST,
                            entity,
                            Map.class
                    );

            List candidates =
                    (List) response.getBody().get("candidates");

            Map candidate =
                    (Map) candidates.get(0);

            Map content =
                    (Map) candidate.get("content");

            List parts =
                    (List) content.get("parts");

            Map part =
                    (Map) parts.get(0);

            return part.get("text").toString().trim();

        } catch (Exception e) {

            return "Unable to predict severity at the moment.";

        }
    }
}