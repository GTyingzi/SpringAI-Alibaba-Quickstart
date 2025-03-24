package com.yingzi.advisorMemory.controller;


//import com.alibaba.cloud.ai.memory.redis.RedisChatMemory;
//import com.alibaba.cloud.ai.memory.mysql.MysqlChatMemory;
import com.yingzi.advisorMemory.component.MysqlChatMemory;
import com.yingzi.advisorMemory.component.RedisChatMemory;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @author yingzi
 * @date 2025/3/23:10:47
 */
@RestController
@RequestMapping("/advisor")
public class AdvisorController {

    private final ChatClient chatClient;
    private final InMemoryChatMemory chatMemory = new InMemoryChatMemory();
    private final int CHAT_MEMORY_RETRIEVE_SIZE = 100;
    private final String MYSQL_USER = "root";
    private final String MYSQL_PASSWORD = "root";
    private final String MYSQL_URL = "127.0.0.1:3306";



    public AdvisorController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory)
                )
                .build();
    }

    @GetMapping("/in-memory")
    public Flux<String> memory(
            @RequestParam("prompt") String prompt,
            @RequestParam("chatId") String chatId,
            HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE))
                .stream().content();
    }

    @GetMapping("/mysql")
    public Flux<String> mysql(
            @RequestParam("prompt") String prompt,
            @RequestParam("chatId") String chatId,
            HttpServletResponse response
    ) {

        response.setCharacterEncoding("UTF-8");

        return chatClient.prompt(prompt).advisors(
                new MessageChatMemoryAdvisor(new MysqlChatMemory(
                        // 填入 mysql 连接参数
                        MYSQL_USER, MYSQL_PASSWORD, MYSQL_URL
                ))
        ).advisors(
                a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
        ).stream().content();
    }

    @GetMapping("/redis")
    public Flux<String> redis(
            @RequestParam("prompt") String prompt,
            @RequestParam("chatId") String chatId,
            HttpServletResponse response
    ) {

        response.setCharacterEncoding("UTF-8");

        return chatClient.prompt(prompt).advisors(
                new MessageChatMemoryAdvisor(new RedisChatMemory(
                        // redis 数据库参数
                ))
        ).advisors(
                a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
        ).stream().content();
    }

}
