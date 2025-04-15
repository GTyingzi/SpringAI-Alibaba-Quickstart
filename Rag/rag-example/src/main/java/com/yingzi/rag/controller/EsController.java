package com.yingzi.rag.controller;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author yingzi
 * @date 2025/4/15:23:01
 */
@RestController
@RequestMapping("/es")
public class EsController {

    private final ElasticsearchVectorStore elasticsearchVectorStore;

    public EsController(ElasticsearchVectorStore elasticsearchVectorStore) {
        this.elasticsearchVectorStore = elasticsearchVectorStore;
    }

    @RequestMapping("/import")
    public void importData() {
        List<Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));
        elasticsearchVectorStore.add(documents);
    }

    @RequestMapping("/search")
    public List<Document> search() {
        return elasticsearchVectorStore.similaritySearch(SearchRequest
                .builder()
                .query("Spring")
                .topK(5)
                .build());
    }
}
