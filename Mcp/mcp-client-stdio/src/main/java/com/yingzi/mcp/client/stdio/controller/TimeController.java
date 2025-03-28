package com.yingzi.mcp.client.stdio.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yingzi
 * @date 2025/3/28:09:17
 */
@RestController
@RequestMapping("/time")
public class TimeController {

    private final ChatClient dashScopeChatClient;
    private final ToolCallbackProvider tools;

    public TimeController(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools) {
        this.dashScopeChatClient = chatClientBuilder.build();
        this.tools = tools;
    }

    /**
     * 无工具版
     */
    @GetMapping("/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "请告诉我现在北京时间几点了") String query) {
        return dashScopeChatClient.prompt(query).call().content();
    }

    /**
     * 调用工具版
     */
    @GetMapping("/chat-tool")
    public String chatTranslateFunction(@RequestParam(value = "query", defaultValue = "请告诉我现在北京时间几点了") String query) {
        return dashScopeChatClient.prompt(query).tools(tools).call().content();
    }
}
