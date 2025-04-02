package com.yingzi.structedOutput.controller;

import com.alibaba.cloud.ai.dashscope.api.DashScopeResponseFormat;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yingzi
 * @date 2025/4/2:17:46
 */
@RestController
@RequestMapping("/json")
public class JsonController {

    private final ChatClient chatClient;
    private final DashScopeResponseFormat responseFormat;

    public JsonController(ChatClient.Builder builder) {
        // AI模型内置支持JSON模式
        DashScopeResponseFormat responseFormat = new DashScopeResponseFormat();
        responseFormat.setType(DashScopeResponseFormat.Type.JSON_OBJECT);

        this.responseFormat = responseFormat;
        this.chatClient = builder
                .build();
    }

    @GetMapping("/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "请以JSON格式介绍你自己") String query) {
        return chatClient.prompt(query).call().content();
    }

    @GetMapping("/chat-format")
    public String simpleChatFormat(@RequestParam(value = "query", defaultValue = "请以JSON格式介绍你自己") String query) {
        return chatClient.prompt(query)
                .options(
                        DashScopeChatOptions.builder()
                                .withTopP(0.7)
                                .withResponseFormat(responseFormat)
                                .build()
                )
                .call().content();
    }
}
