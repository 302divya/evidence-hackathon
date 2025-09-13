package com.hackathon.evidence_bot_backend.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

@Data
public class QueryRequest {

    @NotBlank(message = "Query text cannot be blank")
    private String query;       // Natural language query

    private String repoOwner;   // Optional
    private String repoName;    // Optional
    private Integer prNumber;   // Optional
    private String jiraTicket;  // Optional
    private String userName;    // Optional

    // Use LocalDate for typed date with ISO format yyyy-MM-dd
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;   // Optional

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;     // Optional

    // Custom method to check if key fields have valid data
    public boolean hasValidData() {
        return (prNumber != null)
                || (repoOwner != null && !repoOwner.isEmpty())
                || (repoName != null && !repoName.isEmpty())
                || (jiraTicket != null && !jiraTicket.isEmpty());
    }
}
