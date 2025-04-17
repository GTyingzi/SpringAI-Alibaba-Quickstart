package com.yingzi.rag.simple.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yingzi
 * @date 2025/4/16:19:11
 */
@RestController
@RequestMapping("/simple")
public class SimpleController {

    private static final Logger logger = LoggerFactory.getLogger(SimpleController.class);
    private final SimpleVectorStore simpleVectorStore;
    private final ChatClient chatClient;

    public SimpleController(EmbeddingModel embeddingModel, ChatClient.Builder builder) {
        this.simpleVectorStore = SimpleVectorStore
                .builder(embeddingModel).build();
        this.chatClient = builder.build();
    }

    @GetMapping("/add")
    public void add() {
        logger.info("start add data");
        HashMap<String, Object> map = new HashMap<>();
        map.put("year", 2025);
        map.put("name", "yingzi");
        List<Document> documents = List.of(
                new Document("你的姓名是影子，在湖南邵阳过完了前18年，本科阶段在长沙就读，研究生阶段于北京就读"),
                new Document("你的姓名是影子，专业领域包含的数学、前后端、大数据、自然语言处理", Map.of("year", 2024)),
                new Document("你姓名是影子，爱好是发呆、思考、运动", map));
        simpleVectorStore.add(documents);
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，你认识影子这个人吗") String query) {
        logger.info("start chat");
        return chatClient.prompt(query).call().content();
    }

    @GetMapping("/chat-qa-advisor")
    public String chatQaAdvisor(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，你认识影子这个人吗") String query) {
        logger.info("start chat with qa-advisor");
        QuestionAnswerAdvisor questionAnswerAdvisor = new QuestionAnswerAdvisor(this.simpleVectorStore,
                SearchRequest.builder()
                        .topK(6)
                        .build());
        return chatClient.prompt(query)
                .advisors(questionAnswerAdvisor)
                .call().content();
    }

    @GetMapping("/chat-rag-advisor")
    public String chatRagAdvisor(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，你认识影子这个人吗") String query) {
        logger.info("start chat with rag-advisor");
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(simpleVectorStore)
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .build())
                .build();

        return chatClient.prompt(query)
                .advisors(retrievalAugmentationAdvisor)
                .call().content();
    }

}
