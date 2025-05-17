package com.yingzi.graph.write.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yingzi
 * @date 2025/5/17 16:15
 */

public class TitleGeneratorNode implements NodeAction {

    private final ChatClient chatClient;

    public TitleGeneratorNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) {
        String content = (String) state.value("reworded").orElse("");
        String prompt = "请为以下内容生成一个简洁有吸引力的中文标题：\n\n" + content;

        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
        String title = response.getResult().getOutput().getText();

        Map<String, Object> result = new HashMap<>();
        result.put("title", title);
        return result;
    }

}
