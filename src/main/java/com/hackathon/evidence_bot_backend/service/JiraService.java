//package com.hackathon.evidence_bot_backend.service;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.Base64;
//
//@Service
//public class JiraService {
//
//    private final WebClient webClient;
//
//    public JiraService(
//            @Value("${jira.base.url}") String jiraBaseUrl,
//            @Value("${jira.api.email}") String jiraEmail,
//            @Value("${jira.api.token}") String jiraApiToken) {
//
//        String basicAuth = Base64.getEncoder()
//                .encodeToString((jiraEmail + ":" + jiraApiToken).getBytes());
//
//        this.webClient = WebClient.builder()
//                .baseUrl(jiraBaseUrl)
//                .defaultHeader("Authorization", "Basic " + basicAuth)
//                .defaultHeader("Accept", "application/json")
//                .build();
//    }
//
//    public String createJiraIssue(String projectKey, String summary, String description, String issueType) {
//        String body = "{ \"fields\": {"
//                + "\"project\": {\"key\": \"" + projectKey + "\"},"
//                + "\"summary\": \"" + summary + "\","
//                + "\"description\": \"" + description + "\","
//                + "\"issuetype\": {\"name\": \"" + issueType + "\"}"
//                + "} }";
//
//        return webClient.post()
//                .uri("/rest/api/2/issue")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(body)
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//    }
//}
