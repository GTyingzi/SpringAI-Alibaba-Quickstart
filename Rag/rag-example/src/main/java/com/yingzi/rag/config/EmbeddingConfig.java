package com.yingzi.rag.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yingzi
 * @date 2025/4/14:23:02
 */
@Configuration
public class EmbeddingConfig {

    // This can be any EmbeddingModel implementation
//    @Bean
//    public EmbeddingModel embeddingModel() {
//        return new OpenAiEmbeddingModel(new OpenAiApi(System.getenv("OPENAI_API_KEY")));
//    }
}
