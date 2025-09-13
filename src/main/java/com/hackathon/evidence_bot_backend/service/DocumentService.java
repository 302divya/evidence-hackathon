package com.hackathon.evidence_bot_backend.service;

import com.hackathon.evidence_bot_backend.util.ExcelUtils;
import com.hackathon.evidence_bot_backend.util.PdfUtils;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    @Value("${evidence.files.dir}")
    private String filesDir;

    @Data
    public static class DocumentSnippet {
        private String title;
        private String text;
    }

    /**
     * Searches files in the configured directory for documents containing the given query text.
     * Supports text extraction from PDF, CSV, TXT, XLS, XLSX files.
     * Returns snippets of matching text around the first occurrence.
     *
     * @param query The query string to search for.
     * @return List of DocumentSnippet containing title and matched text snippet.
     */
    public List<DocumentSnippet> searchDocuments(String query) {
        List<DocumentSnippet> results = new ArrayList<>();

        try {
            File dir = new File(filesDir);
            if (!dir.exists() || !dir.isDirectory()) {
                logger.warn("Documents directory does not exist or is not a directory: {}", filesDir);
                return results;
            }

            File[] files = dir.listFiles();
            if (files == null) {
                logger.warn("No files found in documents directory: {}", filesDir);
                return results;
            }

            String q = (query == null) ? "" : query.toLowerCase();

            for (File f : files) {
                try {
                    String text = "";

                    if (f.getName().toLowerCase().endsWith(".pdf")) {
                        text = PdfUtils.extractText(f.getAbsolutePath());
                    } else if (f.getName().toLowerCase().endsWith(".csv") || f.getName().toLowerCase().endsWith(".txt")) {
                        text = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
                    } else if (f.getName().toLowerCase().endsWith(".xlsx") || f.getName().toLowerCase().endsWith(".xls")) {
                        text = ExcelUtils.extractText(f.getAbsolutePath());
                    } else {

                        continue;
                    }

                    if (text.toLowerCase().contains(q)) {
                        DocumentSnippet ds = new DocumentSnippet();
                        ds.setTitle(f.getName());

                        int idx = text.toLowerCase().indexOf(q);
                        int start = Math.max(0, idx - 200);
                        int end = Math.min(text.length(), idx + 200);


                        ds.setText(text.substring(start, end).replaceAll("\\s+", " "));

                        results.add(ds);
                    }
                } catch (Exception e) {
                    logger.error("Failed to process file {}: {}", f.getName(), e.getMessage());
                }
            }
        } catch (Exception ex) {
            logger.error("Error searching documents: {}", ex.getMessage());
        }

        logger.info("Documents search completed. Query: '{}', Matches found: {}", query, results.size());
        return results;
    }
}
