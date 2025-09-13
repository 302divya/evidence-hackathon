package com.hackathon.evidence_bot_backend.service;

import com.hackathon.evidence_bot_backend.dto.QueryRequest;
import com.hackathon.evidence_bot_backend.dto.QueryResponse;
import com.hackathon.evidence_bot_backend.model.AuditLog;
import com.hackathon.evidence_bot_backend.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryService {

    private static final Logger logger = LoggerFactory.getLogger(QueryService.class);

    private final NLPService nlpService;
    private final GitHubConnector gitHubConnector;
    private final DocumentService documentService;
    private final ExportService exportService;
    private final AuditLogRepository auditLogRepository;
    private final OpenAiService openAiService;
    private final JiraService jiraService;

    @Value("${evidence.files.dir}")
    private String filesDir;

    private static final int MAX_DOC_SNIPPETS = 10;

    public QueryResponse processQuery(QueryRequest req) {
        QueryResponse response = new QueryResponse();

        if (req.getQuery() == null || req.getQuery().trim().isEmpty()) {
            response.setSummary("Empty query received");
            response.setConfidence("low");
            return response;
        }

        String rewrittenQuery = openAiService.rewriteQuery(req.getQuery());
        req.setQuery(rewrittenQuery);

        QueryRequest parsedRequest = nlpService.parseQuery(rewrittenQuery);

        List<QueryResponse.EvidenceItem> evidenceItems = new ArrayList<>();
        String summary = "";
        String confidence = "low";

        try {
            if (parsedRequest.getPrNumber() != null
                    && parsedRequest.getRepoOwner() != null && !parsedRequest.getRepoOwner().isEmpty()
                    && parsedRequest.getRepoName() != null && !parsedRequest.getRepoName().isEmpty()) {

                var prData = gitHubConnector.fetchPR(parsedRequest.getRepoOwner(), parsedRequest.getRepoName(), parsedRequest.getPrNumber());

                String mergedBy = prData.containsKey("merged_by") ? prData.get("merged_by").toString() : "unknown";
                String mergedAt = prData.containsKey("merged_at") ? prData.get("merged_at").toString() : "unknown";

                String prJsonPath = exportService.saveJson(prData, "pr_" + parsedRequest.getPrNumber() + ".json");

                QueryResponse.EvidenceItem ev = new QueryResponse.EvidenceItem();
                ev.setId("pr-" + parsedRequest.getPrNumber());
                ev.setType("github-pr");
                ev.setDescription("PR JSON and timeline");
                ev.setPath(prJsonPath);
                evidenceItems.add(ev);

                summary = "PR #" + parsedRequest.getPrNumber() + " in " + parsedRequest.getRepoOwner() + "/" + parsedRequest.getRepoName()
                        + " merged by " + mergedBy + " at " + mergedAt;
                confidence = "medium";
            }

            var docs = documentService.searchDocuments(parsedRequest.getQuery());
            if (!docs.isEmpty()) {
                int idx = 1;
                for (var d : docs) {
                    if (idx > MAX_DOC_SNIPPETS) break;
                    String path = exportService.saveText(d.getText(), "doc_snippet_" + idx + ".txt");
                    QueryResponse.EvidenceItem ev = new QueryResponse.EvidenceItem();
                    ev.setId("doc-" + idx);
                    ev.setType("document");
                    ev.setDescription(d.getTitle());
                    ev.setPath(path);
                    evidenceItems.add(ev);
                    idx++;
                }
                if (summary.isEmpty()) {
                    summary = "Found " + Math.min(docs.size(), MAX_DOC_SNIPPETS) + " document snippets relevant to the query.";
                } else {
                    summary += " Also found " + Math.min(docs.size(), MAX_DOC_SNIPPETS) + " relevant document snippets.";
                }
                confidence = "medium";
            }

            if ("medium".equals(confidence)) {
                String jiraSummary = "Query: " + parsedRequest.getQuery();
                String jiraDescription = "User: " + (parsedRequest.getUserName() != null ? parsedRequest.getUserName() : "N/A") + "\n"
                        + "Repo: " + parsedRequest.getRepoOwner() + "/" + parsedRequest.getRepoName() + "\n"
                        + "PR Number: " + (parsedRequest.getPrNumber() != null ? parsedRequest.getPrNumber() : "N/A") + "\n"
                        + "Jira Ticket: " + (parsedRequest.getJiraTicket() != null ? parsedRequest.getJiraTicket() : "None") + "\n"
                        + "Date Range: " + parsedRequest.getStartDate() + " to " + parsedRequest.getEndDate();

                String jiraProjectKey = "SCRUM";

                String jiraResponse = jiraService.createJiraIssue(jiraProjectKey, jiraSummary, jiraDescription, "Task");
                logger.info("Created Jira ticket: {}", jiraResponse);
            }

            String zipPath = exportService.bundleToZip(evidenceItems, "evidence_bundle_" + Instant.now().toEpochMilli() + ".zip");
            response.setZipDownloadUrl(zipPath);
            response.setSummary(summary);
            response.setEvidence(evidenceItems);
            response.setConfidence(confidence);

            AuditLog log = new AuditLog();
            log.setQuery(parsedRequest.getQuery());
            log.setCreatedAt(Instant.now());
            log.setResultSummary(summary);
            auditLogRepository.save(log);

            return response;

        } catch (Exception ex) {
            logger.error("Error processing query", ex);
            response.setSummary("Error processing query: " + ex.getMessage());
            response.setConfidence("low");
            return response;
        }
    }
}
