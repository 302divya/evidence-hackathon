package com.hackathon.evidence_bot_backend.util;

import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfUtils {

    public static String extractText(String filePath) throws Exception {
        try (PDDocument doc = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }
}
