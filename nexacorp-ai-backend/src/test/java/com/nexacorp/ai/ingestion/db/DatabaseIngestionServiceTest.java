package com.nexacorp.ai.ingestion.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DatabaseIngestionServiceTest {
    @Autowired
    DatabaseIngestionService databaseIngestionService;

    @Test
    public void testDatabaseIngestionService() {
        databaseIngestionService.ingestFaqs();
        databaseIngestionService.ingestReleaseNotes();
        databaseIngestionService.ingestAnnouncements();
    }

}
