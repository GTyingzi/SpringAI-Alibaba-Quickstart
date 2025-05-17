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

public class RewordingNode implements NodeAction {

    private final ChatClient chatClient;

    public RewordingNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String summary = (String) state.value("summary").orElse("");
        String prompt = "请将以下摘要用更优美、生动的语言改写，同时保持信息不变：\n\n" + summary;

        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
        String reworded = response.getResult().getOutput().getText();

        Map<String, Object> result = new HashMap<>();
        result.put("reworded", reworded);
        return result;
    }

}
