package com.hackathon.evidence_bot_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class JiraService {

    private final WebClient webClient;

    public JiraService(
            @Value("${jira.base.url}") String jiraBaseUrl,
            @Value("${jira.api.email}") String jiraEmail,
            @Value("${jira.api.token}") String jiraApiToken) {

        String basicAuth = Base64.getEncoder()
                .encodeToString((jiraEmail + ":" + jiraApiToken).getBytes());

        this.webClient = WebClient.builder()
                .baseUrl(jiraBaseUrl)
                .defaultHeader("Authorization", "Basic " + basicAuth)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    /**
     * Create a Jira issue in the specified project.
     *
     * @param projectKey Jira project key
     * @param summary Issue summary/title
     * @param description Issue description
     * @param issueType Type of issue (e.g., Task, Bug)
     * @return Jira API response as String (JSON)
     */
    public String createJiraIssue(String projectKey, String summary, String description, String issueType) {
        Map<String, Object> fields = new HashMap<>();
        Map<String, String> project = new HashMap<>();
        project.put("key", projectKey);

        Map<String, String> issueTypeMap = new HashMap<>();
        issueTypeMap.put("name", issueType);

        fields.put("project", project);
        fields.put("summary", summary);
        fields.put("description", description);
        fields.put("issuetype", issueTypeMap);

        Map<String, Object> body = new HashMap<>();
        body.put("fields", fields);

        return webClient.post()
                .uri("/rest/api/2/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
