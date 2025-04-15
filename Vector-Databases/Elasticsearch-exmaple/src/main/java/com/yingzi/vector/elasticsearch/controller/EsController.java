package com.yingzi.vector.elasticsearch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yingzi
 * @date 2025/4/15:23:09
 */
@RestController
@RequestMapping("/es")
public class EsController {

    private static final Logger logger = LoggerFactory.getLogger(EsController.class);


    private final ElasticsearchVectorStore elasticsearchVectorStore;

    @Autowired
    public EsController(@Qualifier("vectorStoreCustom") ElasticsearchVectorStore elasticsearchVectorStore) {
        this.elasticsearchVectorStore = elasticsearchVectorStore;
    }

    @GetMapping("/import")
    public void importData() {
        logger.info("start import data");

        HashMap<String, Object> map = new HashMap<>();
        map.put("id", "12345");
        map.put("year", "2025");
        map.put("name", "yingzi");
        List<Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", map),
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("key1", "meta1")));
        elasticsearchVectorStore.add(documents);
    }

    @GetMapping("/search")
    public List<Document> search() {
        logger.info("start search data");
        return elasticsearchVectorStore.similaritySearch(SearchRequest
                .builder()
                .query("Spring")
                .topK(1)
                .build());
    }
}
