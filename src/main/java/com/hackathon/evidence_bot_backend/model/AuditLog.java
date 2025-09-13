package com.hackathon.evidence_bot_backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "audit_log")
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 4000)
    private String query;

    @Column(length = 4000)
    private String resultSummary;

    private Instant createdAt;
}
