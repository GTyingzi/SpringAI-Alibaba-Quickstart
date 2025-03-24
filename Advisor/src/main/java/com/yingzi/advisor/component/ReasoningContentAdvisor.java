package com.yingzi.advisor.component;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author yingzi
 * @date 2025/3/21:17:36
 */
public class ReasoningContentAdvisor implements BaseAdvisor {

    private static final Logger logger = LoggerFactory.getLogger(ReasoningContentAdvisor.class);

    private final int order;

    public ReasoningContentAdvisor(Integer order) {
        this.order = order != null ? order : 0;
    }

    @NotNull
    @Override
    public AdvisedRequest before(@NotNull AdvisedRequest request) {

        return request;
    }

    @NotNull
    @Override
    public AdvisedResponse after(AdvisedResponse advisedResponse) {

        ChatResponse resp = advisedResponse.response();
        if (Objects.isNull(resp)) {

            return advisedResponse;
        }

        logger.info(String.valueOf(resp.getResults().get(0).getOutput().getMetadata()));
        String reasoningContent = String.valueOf(resp.getResults().get(0).getOutput().getMetadata().get("reasoningContent"));

        if (StringUtils.hasText(reasoningContent)) {
            List<Generation> thinkGenerations = resp.getResults().stream()
                    .map(generation -> {
                        AssistantMessage output = generation.getOutput();
                        AssistantMessage thinkAssistantMessage = new AssistantMessage(
                                String.format("<think>%s</think>", reasoningContent) + output.getText(),
                                output.getMetadata(),
                                output.getToolCalls(),
                                output.getMedia()
                        );
                        return new Generation(thinkAssistantMessage, generation.getMetadata());
                    }).toList();

            ChatResponse thinkChatResp = ChatResponse.builder().from(resp).generations(thinkGenerations).build();
            return AdvisedResponse.from(advisedResponse).response(thinkChatResp).build();

        }

        return advisedResponse;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

}
