package com.hackathon.evidence_bot_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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

    /** Create a Jira issue */
    public String createJiraIssue(String projectKey, String summary, String description, String issueType) {
        Map<String, Object> fields = Map.of(
                "project", Map.of("key", projectKey),
                "summary", summary,
                "description", description,
                "issuetype", Map.of("name", issueType)
        );

        Map<String, Object> body = Map.of("fields", fields);

        return webClient.post()
                .uri("/rest/api/2/issue")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /** Search Jira with JQL and expand changelog */
    public Map<String, Object> searchIssuesWithChangelog(String jql) {
        String encodedJql = UriUtils.encode(jql, StandardCharsets.UTF_8);
        String uri = "/rest/api/2/search?jql=" + encodedJql + "&expand=changelog&maxResults=100";

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    /** Tickets closed without going through 'Approved' state */
    public List<Map<String, Object>> findTicketsClosedSkippingApproved(String projectKey) {
        String jql = "project = " + projectKey + " AND status = Done";
        Map<String, Object> results = searchIssuesWithChangelog(jql);

        List<Map<String, Object>> issues = (List<Map<String, Object>>) results.get("issues");
        List<Map<String, Object>> filtered = new ArrayList<>();

        for (Map<String, Object> issue : issues) {
            Map<String, Object> changelog = (Map<String, Object>) issue.get("changelog");
            List<Map<String, Object>> histories = (List<Map<String, Object>>) changelog.get("histories");

            boolean approvedFound = false;
            for (Map<String, Object> history : histories) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) history.get("items");
                for (Map<String, Object> item : items) {
                    if ("status".equals(item.get("field")) && "Approved".equals(item.get("toString"))) {
                        approvedFound = true;
                        break;
                    }
                }
                if (approvedFound) break;
            }

            if (!approvedFound) filtered.add(issue);
        }
        return filtered;
    }

    /** Tickets currently blocked waiting for 'Waiting for Approval' */
    public List<Map<String, Object>> findTicketsWaitingForApproval(String projectKey) {
        String jql = "project = " + projectKey + " AND status = \"Waiting for Approval\"";
        Map<String, Object> results = searchIssuesWithChangelog(jql);
        return (List<Map<String, Object>>) results.get("issues");
    }

    /** Access requests approved in last sprint (between two dates in yyyy-MM-dd) */
    public List<Map<String, Object>> findAccessRequestsApproved(String projectKey, String startDate, String endDate) {
        String jql = String.format("project = %s AND issuetype = \"Access Request\" AND status = Approved AND status changed TO Approved DURING (%s, %s)",
                projectKey, startDate, endDate);

        Map<String, Object> results = searchIssuesWithChangelog(jql);
        return (List<Map<String, Object>>) results.get("issues");
    }

    /** Tickets moved to QA during given date range */
    public List<Map<String, Object>> findTicketsMovedToQA(String projectKey, String startDate, String endDate) {
        String jql = String.format("project = %s AND status changed TO \"In QA\" DURING (%s, %s)",
                projectKey, startDate, endDate);

        Map<String, Object> results = searchIssuesWithChangelog(jql);
        return (List<Map<String, Object>>) results.get("issues");
    }
}
