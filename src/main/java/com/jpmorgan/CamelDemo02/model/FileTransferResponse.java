package com.jpmorgan.CamelDemo02.model;

import java.time.LocalDateTime;

public class FileTransferResponse {
    private String fileId;
    private String fileName;
    private String status;
    private String message;
    private boolean processed;
    private boolean duplicateSkipped;
    private String timestamp;

    public FileTransferResponse() {
        this.timestamp = LocalDateTime.now().toString();
    }

    public FileTransferResponse(String fileId, String fileName, String status, String message, boolean processed, boolean duplicateSkipped) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.status = status;
        this.message = message;
        this.processed = processed;
        this.duplicateSkipped = duplicateSkipped;
        this.timestamp = LocalDateTime.now().toString();
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public boolean isDuplicateSkipped() {
        return duplicateSkipped;
    }

    public void setDuplicateSkipped(boolean duplicateSkipped) {
        this.duplicateSkipped = duplicateSkipped;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
