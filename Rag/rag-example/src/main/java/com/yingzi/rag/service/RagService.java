package com.yingzi.rag.service;

import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * @author yingzi
 * @date 2025/4/13:20:11
 */
public interface RagService {

    void importDocument();

    Flux<ChatResponse> retrieve(String message);

}
