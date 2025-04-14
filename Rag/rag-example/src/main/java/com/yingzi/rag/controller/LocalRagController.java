package com.yingzi.rag.controller;

import com.yingzi.rag.service.impl.LocalRagServiceImpl;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author yingzi
 * @date 2025/4/13:20:12
 */
@RestController
@RequestMapping("/rag/local")
public class LocalRagController {

    private final LocalRagServiceImpl ragService;
    private final ChatModel chatModel;
    public LocalRagController(LocalRagServiceImpl ragService, ChatModel chatModel) {
        this.ragService = ragService;
        this.chatModel = chatModel;
    }

    @RequestMapping("/import/document")
    public void importDocument() {
        ragService.importDocument();
    }

    @RequestMapping("/chat-rag")
    public Flux<String> streamChatRag(@RequestParam(value = "message",
            defaultValue = "how to get start with spring ai alibaba?") String message) {
        return ragService.retrieve(message).map(x -> x.getResult().getOutput().getText());
    }

    @RequestMapping("/chat")
    public Flux<String> streamChat(@RequestParam(value = "message",
            defaultValue = "how to get start with spring ai alibaba?") String message) {
        ChatClient chatClient = ChatClient.builder(chatModel)
                .build();
        return chatClient.prompt().user(message).stream().chatResponse().map(x -> x.getResult().getOutput().getText());
    }
}
