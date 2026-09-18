package com.jpmorgan.CamelDemo02;

import com.jpmorgan.CamelDemo02.model.FileTransferRequest;
import com.jpmorgan.CamelDemo02.model.FileTransferResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileTransferRouteTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testIdempotentFileTransferTrigger() {
        String baseUrl = "http://localhost:" + port + "/api/camel/transfer/trigger";

        FileTransferRequest request = new FileTransferRequest(
                "TEST-ID-100",
                "unit_test_file.txt",
                "Hello Camel Idempotent Test Payload"
        );

        // First Call: Should be processed successfully
        ResponseEntity<FileTransferResponse> response1 = restTemplate.postForEntity(baseUrl, request, FileTransferResponse.class);
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertNotNull(response1.getBody());
        assertEquals("SUCCESS", response1.getBody().getStatus());
        assertTrue(response1.getBody().isProcessed());
        assertFalse(response1.getBody().isDuplicateSkipped());

        // Second Call with exact same fileId: Should be detected as DUPLICATE and SKIPPED (Idempotent)
        ResponseEntity<FileTransferResponse> response2 = restTemplate.postForEntity(baseUrl, request, FileTransferResponse.class);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertNotNull(response2.getBody());
        assertEquals("SKIPPED_DUPLICATE", response2.getBody().getStatus());
        assertFalse(response2.getBody().isProcessed());
        assertTrue(response2.getBody().isDuplicateSkipped());
    }
}
