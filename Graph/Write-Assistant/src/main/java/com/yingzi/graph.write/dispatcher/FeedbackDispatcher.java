package com.yingzi.graph.write.dispatcher;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.EdgeAction;

/**
 * @author yingzi
 * @date 2025/5/17 16:16
 */

public class FeedbackDispatcher implements EdgeAction {

    @Override
    public String apply(OverAllState state) {
        String feedback = (String) state.value("summary_feedback").orElse("");
        if (feedback.contains("positive")) {
            return "positive";
        }
        return "negative";
    }

}
