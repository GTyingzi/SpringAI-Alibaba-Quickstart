package org.springframework.ai.chat.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.EmptyRateLimit;
import org.springframework.ai.chat.metadata.PromptMetadata;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

/**
 * @author yingzi
 * @date 2025/4/8:10:02
 */
public class MessageAggregator {
    private static final Logger logger = LoggerFactory.getLogger(MessageAggregator.class);

    public MessageAggregator() {
    }

    public Flux<AdvisedResponse> aggregateAdvisedResponse(Flux<AdvisedResponse> advisedResponses, Consumer<AdvisedResponse> aggregationHandler) {
        AtomicReference<Map<String, Object>> adviseContext = new AtomicReference(new HashMap());
        return (new MessageAggregator()).aggregate(advisedResponses.map((ar) -> {
            ((Map)adviseContext.get()).putAll(ar.adviseContext());
            return ar.response();
        }), (aggregatedChatResponse) -> {
            AdvisedResponse aggregatedAdvisedResponse = AdvisedResponse.builder().response(aggregatedChatResponse).adviseContext((Map)adviseContext.get()).build();
            aggregationHandler.accept(aggregatedAdvisedResponse);
        }).map((cr) -> {
            return new AdvisedResponse(cr, (Map)adviseContext.get());
        });
    }

    public Flux<ChatResponse> aggregate(Flux<ChatResponse> fluxChatResponse, Consumer<ChatResponse> onAggregationComplete) {
        AtomicReference<StringBuilder> messageTextContentRef = new AtomicReference(new StringBuilder());
        AtomicReference<Map<String, Object>> messageMetadataMapRef = new AtomicReference();
        AtomicReference<List<AssistantMessage.ToolCall>> messageToolCallsRef = new AtomicReference(List.of());
        AtomicReference<ChatGenerationMetadata> generationMetadataRef = new AtomicReference(ChatGenerationMetadata.NULL);
        AtomicReference<Integer> metadataUsagePromptTokensRef = new AtomicReference(0);
        AtomicReference<Integer> metadataUsageGenerationTokensRef = new AtomicReference(0);
        AtomicReference<Integer> metadataUsageTotalTokensRef = new AtomicReference(0);
        AtomicReference<PromptMetadata> metadataPromptMetadataRef = new AtomicReference(PromptMetadata.empty());
        AtomicReference<RateLimit> metadataRateLimitRef = new AtomicReference(new EmptyRateLimit());
        AtomicReference<String> metadataIdRef = new AtomicReference("");
        AtomicReference<String> metadataModelRef = new AtomicReference("");
        return fluxChatResponse.doOnSubscribe((subscription) -> {
            messageTextContentRef.set(new StringBuilder());
            messageMetadataMapRef.set(new HashMap());
            metadataIdRef.set("");
            metadataModelRef.set("");
            metadataUsagePromptTokensRef.set(0);
            metadataUsageGenerationTokensRef.set(0);
            metadataUsageTotalTokensRef.set(0);
            metadataPromptMetadataRef.set(PromptMetadata.empty());
            metadataRateLimitRef.set(new EmptyRateLimit());
        }).doOnNext((chatResponse) -> {
            if (chatResponse.getResult() != null) {
                if (chatResponse.getResult().getMetadata() != null && chatResponse.getResult().getMetadata() != ChatGenerationMetadata.NULL) {
                    generationMetadataRef.set(chatResponse.getResult().getMetadata());
                }

                if (chatResponse.getResult().getOutput().getText() != null) {
                    ((StringBuilder)messageTextContentRef.get()).append(chatResponse.getResult().getOutput().getText());
                }

                if (chatResponse.getResult().getOutput().getToolCalls() != null) {
                    messageToolCallsRef.set(chatResponse.getResult().getOutput().getToolCalls());
                }

                if (chatResponse.getResult().getOutput().getMetadata() != null) {
                    ((Map)messageMetadataMapRef.get()).putAll(chatResponse.getResult().getOutput().getMetadata());
                }
            }

            if (chatResponse.getMetadata() != null) {
                if (chatResponse.getMetadata().getUsage() != null) {
                    Usage usage = chatResponse.getMetadata().getUsage();
                    metadataUsagePromptTokensRef.set(usage.getPromptTokens() > 0 ? usage.getPromptTokens() : (Integer)metadataUsagePromptTokensRef.get());
                    metadataUsageGenerationTokensRef.set(usage.getCompletionTokens() > 0 ? usage.getCompletionTokens() : (Integer)metadataUsageGenerationTokensRef.get());
                    metadataUsageTotalTokensRef.set(usage.getTotalTokens() > 0 ? usage.getTotalTokens() : (Integer)metadataUsageTotalTokensRef.get());
                }

                if (chatResponse.getMetadata().getPromptMetadata() != null && chatResponse.getMetadata().getPromptMetadata().iterator().hasNext()) {
                    metadataPromptMetadataRef.set(chatResponse.getMetadata().getPromptMetadata());
                }

                if (chatResponse.getMetadata().getRateLimit() != null && !(metadataRateLimitRef.get() instanceof EmptyRateLimit)) {
                    metadataRateLimitRef.set(chatResponse.getMetadata().getRateLimit());
                }

                if (StringUtils.hasText(chatResponse.getMetadata().getId())) {
                    metadataIdRef.set(chatResponse.getMetadata().getId());
                }

                if (StringUtils.hasText(chatResponse.getMetadata().getModel())) {
                    metadataModelRef.set(chatResponse.getMetadata().getModel());
                }
            }

        }).doOnComplete(() -> {
            DefaultUsage usage = new DefaultUsage((Integer)metadataUsagePromptTokensRef.get(), (Integer)metadataUsageGenerationTokensRef.get(), (Integer)metadataUsageTotalTokensRef.get());
            ChatResponseMetadata chatResponseMetadata = ChatResponseMetadata.builder().id((String)metadataIdRef.get()).model((String)metadataModelRef.get()).rateLimit((RateLimit)metadataRateLimitRef.get()).usage(usage).promptMetadata((PromptMetadata)metadataPromptMetadataRef.get()).build();
            onAggregationComplete.accept(new ChatResponse(List.of(new Generation(new AssistantMessage(((StringBuilder)messageTextContentRef.get()).toString(), (Map)messageMetadataMapRef.get(), (List)messageToolCallsRef.get()), (ChatGenerationMetadata)generationMetadataRef.get())), chatResponseMetadata));
            messageTextContentRef.set(new StringBuilder());
            messageMetadataMapRef.set(new HashMap());
            messageToolCallsRef.set(List.of());
            metadataIdRef.set("");
            metadataModelRef.set("");
            metadataUsagePromptTokensRef.set(0);
            metadataUsageGenerationTokensRef.set(0);
            metadataUsageTotalTokensRef.set(0);
            metadataPromptMetadataRef.set(PromptMetadata.empty());
            metadataRateLimitRef.set(new EmptyRateLimit());
        }).doOnError((e) -> {
            logger.error("Aggregation Error", e);
        });
    }

    public static record DefaultUsage(Integer promptTokens, Integer completionTokens, Integer totalTokens) implements Usage {
        public DefaultUsage(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalTokens = totalTokens;
        }

        public Integer getPromptTokens() {
            return this.promptTokens();
        }

        public Integer getCompletionTokens() {
            return this.completionTokens();
        }

        public Integer getTotalTokens() {
            return this.totalTokens();
        }

        public Map<String, Integer> getNativeUsage() {
            Map<String, Integer> usage = new HashMap();
            usage.put("promptTokens", this.promptTokens());
            usage.put("completionTokens", this.completionTokens());
            usage.put("totalTokens", this.totalTokens());
            return usage;
        }

        public Integer promptTokens() {
            return this.promptTokens;
        }

        public Integer completionTokens() {
            return this.completionTokens;
        }

        public Integer totalTokens() {
            return this.totalTokens;
        }
    }
}
