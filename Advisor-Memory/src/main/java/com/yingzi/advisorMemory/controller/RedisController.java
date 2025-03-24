package com.yingzi.advisorMemory.controller;

import com.yingzi.advisorMemory.component.RedisChatMemory;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import java.util.*;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @author yingzi
 * @date 2025/3/23:21:07
 */
@RestController
@RequestMapping("/redis")
public class RedisController {

    private final ChatClient.Builder builder;

    private ChatClient chatClient;

    private RedisChatMemory redisChatMemory;

    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.port}")
    private int redisPort;

    public RedisController(ChatClient.Builder builder) {
        this.builder = builder;
    }

    @PostConstruct
    public void init() {
        redisChatMemory = new RedisChatMemory(redisHost, redisPort, null);
        chatClient = builder
                .defaultAdvisors(new MessageChatMemoryAdvisor(redisChatMemory))
                .build();
    }

    /**
     * 增
     */

    @GetMapping("/add")
    public Flux<String> redis(
            @RequestParam("prompt") String prompt,
            @RequestParam("chatId") String chatId) {
        return chatClient.prompt(prompt).advisors(a -> a
                .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
        ).stream().content();
    }

    /**
     * 删
     */
    @GetMapping("/clear")
    public void clear(@RequestParam("chatId") String chatId) {
        if (Objects.isNull(chatId)) {
            throw new RuntimeException("chatId is null");
        }
        redisChatMemory.clear(chatId);
    }

    /**
     * 改
     */
    @GetMapping("/modify")
    public void modify(@RequestParam("chatId") String chatId, @RequestParam("messages") String messages) {
        if (Objects.isNull(chatId)) {
            throw new RuntimeException("chatId is null");
        }
        redisChatMemory.updateMessageById(chatId, messages);
    }

    /**
     * 查
     */
    @GetMapping("/get")
    public List<Message> get(
            @RequestParam("chatId") String chatId,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        return redisChatMemory.get(chatId, size);
    }
}
