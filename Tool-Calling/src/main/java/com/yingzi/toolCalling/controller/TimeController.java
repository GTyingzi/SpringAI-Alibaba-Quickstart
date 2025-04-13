package com.yingzi.toolCalling.controller;

import com.yingzi.toolCalling.advisor.ReasoningContentAdvisor;
import com.yingzi.toolCalling.component.time.TimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @author yingzi
 * @date 2025/3/25:14:43
 */
@RestController
@RequestMapping("/time")
public class TimeController {

    private final InMemoryChatMemory chatMemory = new InMemoryChatMemory();
    private final int CHAT_MEMORY_RETRIEVE_SIZE = 100;

    private final int CHAT_ID = 1000;

    private final int STREAM_CHAT_ID = 1001;

    private final ChatClient dashScopeChatClient;

    public TimeController(ChatClient.Builder chatClientBuilder) {
        this.dashScopeChatClient = chatClientBuilder
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
//                .defaultOptions(ToolCallingChatOptions.builder()
//                        .internalToolExecutionEnabled(false)  // 禁用内部工具执行
//                        .build())
                .build();
    }


    /**
     * 无工具版
     */
    @GetMapping("/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "请告诉我现在北京时间几点了") String query) {
        return dashScopeChatClient.prompt(query).call().content();
    }

    /**
     * 调用工具版 - function
     */
    @GetMapping("/chat-tool-function")
    public String chatTimeFunction(@RequestParam(value = "query", defaultValue = "请告诉我现在北京时间几点了") String query) {
        return dashScopeChatClient.prompt(query).tools("getCityTimeFunction").call().content();
    }

    /**
     * 调用工具版 - method
     */
    @GetMapping("/chat-tool-method")
    public String chatTimeMethod(@RequestParam(value = "query", defaultValue = "请告诉我现在北京时间几点了") String query) {
        return dashScopeChatClient.prompt(query)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, CHAT_ID)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE))
                .tools(new TimeTools())
                .call().content();
    }

    /**
     * 调用工具版 - function - stream
     */
    @GetMapping("/chat-tool-function-stream")
    public Flux<String> chatTimeFunctionStream(@RequestParam(value = "query", defaultValue = "请告诉我现在北京时间几点了") String query) {
        return dashScopeChatClient.prompt(query)
                .tools("getCityTimeFunction")
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, STREAM_CHAT_ID)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE))
                .stream().content();
    }
}
