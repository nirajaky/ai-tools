package com.nexacorp.ai.ingestion.pdf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PdfIngestionServiceTest {
    @Autowired
    PdfIngestionService pdfIngestionService;

    @Test
    void ingestPdfs_forLearning() throws Exception {
        pdfIngestionService.ingestPdfs();
    }
}