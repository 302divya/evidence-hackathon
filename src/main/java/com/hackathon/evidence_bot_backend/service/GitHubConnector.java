package com.hackathon.evidence_bot_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GitHubConnector {

    private static final Logger logger = LoggerFactory.getLogger(GitHubConnector.class);

    private final WebClient webClient;

    public GitHubConnector(@Value("${github.token}") String token) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "token " + token)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .build();
        logger.info("GitHubConnector initialized with provided token.");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchPR(String owner, String repo, int prNumber) {
        Map<String, Object> result = new HashMap<>();
        try {
            // Fetch PR details
            Map<String, Object> prData = webClient.get()
                    .uri("/repos/{owner}/{repo}/pulls/{number}", owner, repo, prNumber)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // Fetch PR reviews
            List<Map<String, Object>> reviews = webClient.get()
                    .uri("/repos/{owner}/{repo}/pulls/{number}/reviews", owner, repo, prNumber)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            if (prData != null) {
                result.put("pr", prData);
                result.put("reviews", reviews);

                Map<String, Object> mergedByMap = (Map<String, Object>) prData.get("merged_by");
                String mergedBy = (mergedByMap != null) ? (String) mergedByMap.get("login") : null;
                String mergedAt = (String) prData.get("merged_at");

                result.put("merged_by", mergedBy != null ? mergedBy : "unknown");
                result.put("merged_at", mergedAt != null ? mergedAt : "unknown");

                logger.info("Fetched PR data for {}/{} PR#{}", owner, repo, prNumber);
            } else {
                logger.warn("No PR data found for {}/{} PR#{}", owner, repo, prNumber);
            }
        } catch (WebClientResponseException e) {
            logger.error("GitHub API error {} while fetching PR: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception ex) {
            logger.error("Unexpected error fetching PR data: {}", ex.getMessage());
        }
        return result;
    }
}
