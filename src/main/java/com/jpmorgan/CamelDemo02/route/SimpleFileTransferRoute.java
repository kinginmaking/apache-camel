package com.jpmorgan.CamelDemo02.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.support.processor.idempotent.MemoryIdempotentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Basic Apache Camel Route for transferring files from an input directory to an output directory.
 * 
 * Key Concepts Demonstrated:
 * 1. RouteBuilder & Java DSL
 * 2. File Component (file:inputDir -> file:outputDir)
 * 3. Logging via Simple Expression Language (${file:name}, ${file:size})
 * 4. Idempotency (preventing duplicate processing of the same file/request)
 */
@Component
public class SimpleFileTransferRoute extends RouteBuilder {

    @Value("${file.input-dir:data/input}")
    private String inputDir;

    @Value("${file.output-dir:data/output}")
    private String outputDir;

    // In-memory repository to store processed file/request IDs for idempotency
    private final MemoryIdempotentRepository idempotentRepository = new MemoryIdempotentRepository();

    @Override
    public void configure() throws Exception {

        // =========================================================================
        // ROUTE 1: Automated File Directory Poller Route
        // Reads files placed in 'data/input', logs details, and moves them to 'data/output'.
        // =========================================================================
        from("file:" + inputDir + "?noop=false&delete=true&idempotent=true")
            .routeId("auto-file-transfer-route")
            .log("--> [AUTO-ROUTE] Detected new file in input folder: ${file:name} (Size: ${file:size} bytes)")
            .process(exchange -> {
                String fileName = exchange.getIn().getHeader("CamelFileName", String.class);
                System.out.println(">>> [Camel Processing] Processing file: " + fileName);
            })
            .log("--> [AUTO-ROUTE] Transferring file '${file:name}' to output folder: " + outputDir)
            .to("file:" + outputDir)
            .log("--> [AUTO-ROUTE] File '${file:name}' successfully transferred!");


        // =========================================================================
        // ROUTE 2: Direct Trigger Route (Called via REST Endpoint)
        // Demonstrates explicit Idempotent Consumer Pattern using custom fileId header.
        // =========================================================================
        from("direct:triggerFileTransfer")
            .routeId("direct-file-transfer-route")
            .log("--> [DIRECT-ROUTE] Received REST trigger request for File ID: ${header.fileId}")
            
            // Idempotent Filter: Ensures identical fileId is processed ONLY ONCE
            .idempotentConsumer(header("fileId"), idempotentRepository)
                .skipDuplicate(true) // Skip duplicate calls cleanly
                
                // Block executed ONLY when fileId is NEW (First time)
                .log("--> [DIRECT-ROUTE] File ID '${header.fileId}' is unique. Creating file '${header.fileName}'...")
                .process(exchange -> {
                    String fileName = exchange.getIn().getHeader("fileName", String.class);
                    String content = exchange.getIn().getBody(String.class);
                    
                    // Set standard Camel file header so file component knows destination file name
                    exchange.getIn().setHeader("CamelFileName", fileName);
                    System.out.println(">>> [Direct Processing] Writing content to file: " + fileName);
                })
                .to("file:" + outputDir)
                .setHeader("transferStatus", constant("SUCCESS"))
                .log("--> [DIRECT-ROUTE] File '${header.fileName}' (ID: ${header.fileId}) successfully created in output directory!")
            .end();
    }

    /**
     * Getter for checking if a fileId has already been processed by the idempotent repository.
     */
    public boolean isAlreadyProcessed(String fileId) {
        return idempotentRepository.contains(fileId);
    }
}
