package com.yingzi.advisor.controller;

import com.yingzi.advisor.component.ReasoningContentAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @author yingzi
 * @date 2025/3/21:17:31
 */
@RestController
@RequestMapping("/advisors")
public class AdvisorsController {


    private final ChatClient chatClient;
    private final InMemoryChatMemory chatMemory = new InMemoryChatMemory();
    private final int CHAT_MEMORY_RETRIEVE_SIZE = 100;


    public AdvisorsController(ChatClient.Builder builder) {

        this.chatClient = builder
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory)
                        // 整合 QWQ 的思考过程到输出中
                        ,new ReasoningContentAdvisor(0)
                )
                .build();
    }

    @GetMapping("/chatWithChatMemory")
    public Flux<String> chatWithChatMemory(String chatId, String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE))
                .stream().content();
    }

    @GetMapping("messages")
    public List<Message> getMessages(@RequestParam(value = "chatId") String chatId, @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        if (Objects.isNull(chatId)) {
            throw new RuntimeException("chatId is null");
        }
        return chatMemory.get(chatId,size);

    }

}
