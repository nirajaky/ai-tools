package com.nexacorp.ai.ingestion.wiki;

import com.nexacorp.ai.ingestion.model.IngestedDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WikiIngestionService {
    private static final Logger log = LoggerFactory.getLogger(WikiIngestionService.class);
    private static final String WIKI_DIRECTORY = "data/wiki";

    public List<IngestedDocument> ingestWikiFiles() throws Exception {
        File[] markdownFiles = new File(WIKI_DIRECTORY).listFiles();

        List<IngestedDocument> docs = new ArrayList<>();
        for(File file: markdownFiles){
            docs.add(ingestSingleFile(file));
        }
        return docs;
    }

    private IngestedDocument ingestSingleFile(File file) throws IOException {
//        log.info("Ingesting wiki file: {}", file.getName());

        String content = Files.readString(file.toPath());

//        log.info("----- Wiki Content ({}) -----", file.getName());
//        log.info(content);
        return new IngestedDocument(
                "WIKI",
                content,
                Map.of("fileName", file.getName())
        );
    }

}
