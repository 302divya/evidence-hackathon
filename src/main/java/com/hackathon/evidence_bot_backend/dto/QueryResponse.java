package com.hackathon.evidence_bot_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class QueryResponse {
    private String summary;

    private List<EvidenceItem> evidence;

    private String zipDownloadUrl; // path to ZIP bundle

    private String confidence; // low/medium/high

    @Data
    public static class EvidenceItem {
        private String id;
        private String type;
        private String description;
        private String path; // local file path or link

        // Optional: add fields like created date, size etc. if needed later
    }
}
