package com.jpmorgan.CamelDemo02.controller;

import com.jpmorgan.CamelDemo02.model.FileTransferRequest;
import com.jpmorgan.CamelDemo02.model.FileTransferResponse;
import com.jpmorgan.CamelDemo02.route.SimpleFileTransferRoute;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/camel/transfer")
public class FileTransferController {

    
    private final ProducerTemplate producerTemplate;
    private final SimpleFileTransferRoute fileTransferRoute;

    @Value("${file.input-dir:data/input}")
    private String inputDir;

    public FileTransferController(ProducerTemplate producerTemplate, SimpleFileTransferRoute fileTransferRoute) {
        this.producerTemplate = producerTemplate;
        this.fileTransferRoute = fileTransferRoute;
    }

    /**
     * IDEMPOTENT REST Endpoint to trigger the file transfer route directly via Camel ProducerTemplate.
     * 
     * Request Payload Example:
     * {
     *   "fileId": "DOC-1001",
     *   "fileName": "invoice_1001.txt",
     *   "content": "Invoice #1001 details: Total $500"
     * }
     */
    @PostMapping("/trigger")
    public ResponseEntity<FileTransferResponse> triggerFileTransfer(@RequestBody FileTransferRequest request) {
        String fileId = request.getFileId();
        String fileName = request.getFileName();

        if (fileId == null || fileId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                new FileTransferResponse(null, fileName, "FAILED", "fileId is required for idempotent call", false, false)
            );
        }

        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "file_" + fileId + ".txt";
        }

        // Check if fileId was already processed to provide instant idempotent status feedback
        boolean wasAlreadyProcessed = fileTransferRoute.isAlreadyProcessed(fileId);

        Map<String, Object> headers = new HashMap<>();
        headers.put("fileId", fileId);
        headers.put("fileName", fileName);

        // Send payload and headers to Camel's direct:triggerFileTransfer route
        producerTemplate.sendBodyAndHeaders("direct:triggerFileTransfer", request.getContent(), headers);

        if (wasAlreadyProcessed) {
            return ResponseEntity.ok(new FileTransferResponse(
                fileId,
                fileName,
                "SKIPPED_DUPLICATE",
                "Idempotent trigger: File ID '" + fileId + "' was previously processed. Skipped duplicate transfer.",
                false,
                true
            ));
        } else {
            return ResponseEntity.ok(new FileTransferResponse(
                fileId,
                fileName,
                "SUCCESS",
                "File ID '" + fileId + "' successfully processed and transferred to output folder by Apache Camel.",
                true,
                false
            ));
        }
    }

    /**
     * Helper Endpoint: Drops a file directly into the input directory.
     * The automated Camel file route will pick it up automatically!
     */
    @PostMapping("/create-file")
    public ResponseEntity<Map<String, Object>> createInputFile(@RequestParam(defaultValue = "test_doc.txt") String fileName,
                                                              @RequestBody(required = false) String content) {
        if (content == null || content.isEmpty()) {
            content = "Sample content generated at " + System.currentTimeMillis();
        }

        File folder = new File(inputDir);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        } catch (IOException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Failed to write file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(err);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "FILE_CREATED");
        response.put("fileName", fileName);
        response.put("inputPath", file.getAbsolutePath());
        response.put("message", "File written to input directory. Apache Camel file polling route will process it automatically!");
        return ResponseEntity.ok(response);
    }

    /**
     * Check if a specific fileId has been processed by the idempotent route.
     */
    @GetMapping("/status/{fileId}")
    public ResponseEntity<Map<String, Object>> checkStatus(@PathVariable String fileId) {
        boolean processed = fileTransferRoute.isAlreadyProcessed(fileId);
        Map<String, Object> res = new HashMap<>();
        res.put("fileId", fileId);
        res.put("processed", processed);
        res.put("status", processed ? "PROCESSED" : "NOT_PROCESSED");
        return ResponseEntity.ok(res);
    }
}
