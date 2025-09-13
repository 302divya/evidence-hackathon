package com.hackathon.evidence_bot_backend.service;

import com.hackathon.evidence_bot_backend.dto.QueryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NLPService {

    private final OpenAiService openAiService;

    public QueryRequest parseQuery(String query) {
        // Use the correct method name rewriteQuery (or add analyzeQuery if needed)
        String rewrittenQuery = openAiService.rewriteQuery(query);

        // Now parse the rewritten query to create QueryRequest, assuming you have logic for that
        QueryRequest result = new QueryRequest();

        // Example: assume your QueryRequest has a setter for raw query
        result.setQuery(rewrittenQuery);

        // Optional fallback for regex or local parsing can be applied here

        return result;
    }
}
