package com.hackathon.evidence_bot_backend.controller;

import com.hackathon.evidence_bot_backend.dto.QueryRequest;
import com.hackathon.evidence_bot_backend.dto.QueryResponse;
import com.hackathon.evidence_bot_backend.service.QueryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // allow React app
@Validated
public class QueryController {

    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);
    private final QueryService queryService;

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> handleQuery(@Valid @RequestBody QueryRequest req) {
        logger.info("Received query: {}", req.getQuery());
        QueryResponse resp = queryService.processQuery(req);
        return ResponseEntity.ok(resp);
    }
}
