package com.hackathon.evidence_bot_backend.service;

import com.hackathon.evidence_bot_backend.dto.QueryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    /**
     * Extracts PR number, repository owner, and repository name using regex.
     */
    public QueryRequest analyzeQuery(String query) {
        QueryRequest req = new QueryRequest();
        req.setQuery(query);


        Pattern pattern = Pattern.compile(
                "(?i)(?:github\\.com/)?([\\w.-]+)/([\\w.-]+).*?(?:/pull/(\\d+)|PR\\s*#?(\\d+))",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (matcher.find()) {
            req.setRepoOwner(matcher.group(1));
            req.setRepoName(matcher.group(2));
            String prNum = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
            if (prNum != null) {
                req.setPrNumber(Integer.parseInt(prNum));
            }
        }

        else {
            Pattern repoPattern = Pattern.compile("(?:github\\.com/)?([\\w.-]+)/([\\w.-]+)", Pattern.CASE_INSENSITIVE);
            Matcher repoMatcher = repoPattern.matcher(query);
            if (repoMatcher.find()) {
                req.setRepoOwner(repoMatcher.group(1));
                req.setRepoName(repoMatcher.group(2));
            }
        }

        return req;
    }
}
