package com.yingzi.graph.write.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yingzi
 * @date 2025/5/17 16:14
 */

public class SummarizerNode implements NodeAction {

    private final ChatClient chatClient;

    public SummarizerNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String text = (String) state.value("original_text").orElse("");
        String prompt = "请对以下中文文本进行简洁明了的摘要：\n\n" + text;

        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
        String summary = response.getResult().getOutput().getText();

        Map<String, Object> result = new HashMap<>();
        result.put("summary", summary);
        return result;
    }

}
