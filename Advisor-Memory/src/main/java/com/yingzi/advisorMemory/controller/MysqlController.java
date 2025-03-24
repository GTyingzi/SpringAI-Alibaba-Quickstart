package com.yingzi.advisorMemory.controller;

import com.yingzi.advisorMemory.component.MysqlChatMemory;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
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
 * @date 2025/3/23:16:40
 */
@RestController
@RequestMapping("/mysql")
public class MysqlController {

    private final ChatClient.Builder builder;

    @Value("${spring.datasource.url}")
    private String mysqlUrl;

    @Value("${spring.datasource.username}")
    private String mysqlUser;

    @Value("${spring.datasource.password}")
    private String mysqlPassword;

    private MysqlChatMemory mysqlChatMemory;
    private ChatClient chatClient;

    public MysqlController(ChatClient.Builder builder) {
        this.builder = builder;
    }

    @PostConstruct
    public void init() {
        mysqlChatMemory = new MysqlChatMemory(mysqlUser, mysqlPassword, mysqlUrl);
        chatClient = builder
                .defaultAdvisors(new MessageChatMemoryAdvisor(mysqlChatMemory))
                .build();
    }

    /**
     * 增
     */
    @GetMapping("/add")
    public Flux<String> mysql(
            @RequestParam("prompt") String prompt,
            @RequestParam("chatId") String chatId,
            HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");

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
        mysqlChatMemory.clear(chatId);
    }

    /**
     * 改
     */
    @GetMapping("/modify")
    public void modify(@RequestParam("chatId") String chatId, @RequestParam("messages") String messages) {
        if (Objects.isNull(chatId)) {
            throw new RuntimeException("chatId is null");
        }
        mysqlChatMemory.updateMessageById(chatId, messages);
    }

    /**
     * 查
     */
    @GetMapping("/get")
    public List<Message> get(
            @RequestParam("chatId") String chatId,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        if (Objects.isNull(chatId)) {
            throw new RuntimeException("chatId is null");
        }
        return mysqlChatMemory.get(chatId, size);
    }


}
