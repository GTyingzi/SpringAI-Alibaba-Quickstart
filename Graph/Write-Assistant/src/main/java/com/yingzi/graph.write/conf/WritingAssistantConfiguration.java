package com.yingzi.graph.write.conf;

import com.alibaba.cloud.ai.graph.*;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import com.yingzi.graph.write.dispatcher.FeedbackDispatcher;
import com.yingzi.graph.write.node.RewordingNode;
import com.yingzi.graph.write.node.SummarizerNode;
import com.yingzi.graph.write.node.SummaryFeedbackClassifierNode;
import com.yingzi.graph.write.node.TitleGeneratorNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

/**
 * @author yingzi
 * @date 2025/5/17 16:05
 */

@Configuration
public class WritingAssistantConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(WritingAssistantConfiguration.class);

    @Bean
    public StateGraph writingAssistantGraph(ChatModel chatModel) throws GraphStateException {

        ChatClient chatClient = ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();

        OverAllStateFactory stateFactory = () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("original_text", new ReplaceStrategy());
            state.registerKeyAndStrategy("summary", new ReplaceStrategy());
            state.registerKeyAndStrategy("summary_feedback", new ReplaceStrategy());
            state.registerKeyAndStrategy("reworded", new ReplaceStrategy());
            state.registerKeyAndStrategy("title", new ReplaceStrategy());
            return state;
        };

        StateGraph graph = new StateGraph("Writing Assistant with Feedback Loop", stateFactory)
                .addNode("summarizer", node_async(new SummarizerNode(chatClient)))
                .addNode("feedback_classifier", node_async(new SummaryFeedbackClassifierNode(chatClient, "summary")))
                .addNode("reworder", node_async(new RewordingNode(chatClient)))
                .addNode("title_generator", node_async(new TitleGeneratorNode(chatClient)))

                .addEdge(START, "summarizer")
                .addEdge("summarizer", "feedback_classifier")
                .addConditionalEdges("feedback_classifier", edge_async(new FeedbackDispatcher()),
                        Map.of("positive", "reworder", "negative", "summarizer"))
                .addEdge("reworder", "title_generator")
                .addEdge("title_generator", END);

        // 添加 PlantUML 打印
        GraphRepresentation representation = graph.getGraph(GraphRepresentation.Type.PLANTUML,
                "writing assistant flow");
        logger.info("\n=== Writing Assistant UML Flow ===");
        logger.info(representation.content());
        logger.info("==================================\n");

        return graph;
    }
}
