package com.hackathon.evidence_bot_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public OpenAiService(WebClient.Builder webClientBuilder,
                         @Value("${openai.api.url}") String apiUrl) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
    }

    /**
     * Rewrites or clarifies the original query using OpenAI chat completion API.
     *
     * @param originalQuery Raw user query.
     * @return Rewritten query or original if API call fails.
     */
    public String rewriteQuery(String originalQuery) {
        String prompt = "Rewrite this audit-related query to be clear and specific:\n\"" + originalQuery + "\"";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4");
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("max_tokens", 60);
        requestBody.put("temperature", 0.7);

        try {
            Map response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null && message.containsKey("content")) {
                        String rewrittenText = (String) message.get("content");
                        if (rewrittenText != null && !rewrittenText.trim().isEmpty()) {
                            return rewrittenText.trim();
                        }
                    }
                }
            }
        } catch (WebClientResponseException e) {
            System.err.println("OpenAI API error: " + e.getRawStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception ex) {
            System.err.println("Error calling OpenAI API: " + ex.getMessage());
        }
        // Fallback to the original query if rewrite fails
        return originalQuery;
    }
}
