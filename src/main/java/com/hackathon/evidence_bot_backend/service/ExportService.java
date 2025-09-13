package com.hackathon.evidence_bot_backend.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.evidence_bot_backend.dto.QueryResponse;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.FileOutputStream;

@Service
public class ExportService {

    @Value("${evidence.files.dir}")
    private String filesDir;

    private final ObjectMapper mapper = new ObjectMapper();

    public String saveText(String text, String filename) throws Exception {
        File dir = new File(filesDir);
        if (!dir.exists()) dir.mkdirs();
        File f = new File(dir, filename);
        FileUtils.writeStringToFile(f, text, StandardCharsets.UTF_8);
        return f.getAbsolutePath();
    }

    public String saveJson(Object obj, String filename) throws Exception {
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        return saveText(json, filename);
    }

    public String bundleToZip(List<QueryResponse.EvidenceItem> items, String zipFilename) throws Exception {
        File dir = new File(filesDir);
        if (!dir.exists()) dir.mkdirs();
        File zipFile = new File(dir, zipFilename);
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (QueryResponse.EvidenceItem item : items) {
                File f = new File(item.getPath());
                if (!f.exists()) continue;
                zos.putNextEntry(new ZipEntry(f.getName()));
                byte[] bytes = FileUtils.readFileToByteArray(f);
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();
            }
        }
        return zipFile.getAbsolutePath();
    }
}
