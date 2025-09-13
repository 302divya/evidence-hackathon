package com.hackathon.evidence_bot_backend.service;

import com.hackathon.evidence_bot_backend.dto.QueryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NLPService {

    private final OpenAiService openAIService;

    public QueryRequest parseQuery(String query) {
        QueryRequest result = openAIService.analyzeQuery(query);

        if (result == null || !result.hasValidData()) {
            // fallback regex parse
            result = regexParse(query);
            result.setQuery(query);
        }

        return result;
    }

    private QueryRequest regexParse(String query) {
        QueryRequest req = new QueryRequest();
        req.setQuery(query);

        String pattern = "(?i)(?:github\\.com/)?([\\w\\-]+)/([\\w\\-]+).*?(?:/pull/(\\d+)|PR\\s*#?(\\d+))";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(query);

        if (m.find()) {
            req.setRepoOwner(m.group(1));
            req.setRepoName(m.group(2));
            String prNum = m.group(3) != null ? m.group(3) : m.group(4);
            if (prNum != null) {
                req.setPrNumber(Integer.parseInt(prNum));
            }
        }
        return req;
    }
}
