package com.jpmorgan.CamelDemo02.model;

public class FileTransferRequest {
    private String fileId;
    private String fileName;
    private String content;

    public FileTransferRequest() {
    }

    public FileTransferRequest(String fileId, String fileName, String content) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
