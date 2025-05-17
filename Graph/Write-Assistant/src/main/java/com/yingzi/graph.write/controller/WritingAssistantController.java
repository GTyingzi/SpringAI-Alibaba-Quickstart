package com.yingzi.graph.write.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.GraphStateException;
import com.alibaba.cloud.ai.graph.StateGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author yingzi
 * @date 2025/5/17 16:28
 */

@RestController
@RequestMapping("/write")
public class WritingAssistantController {

    private final CompiledGraph compiledGraph;

    @Autowired
    public WritingAssistantController(@Qualifier("writingAssistantGraph") StateGraph writingAssistantGraph)
            throws GraphStateException {
        this.compiledGraph = writingAssistantGraph.compile();
    }

    /**
     * 调用写作助手流程图 示例请求：GET /write?text=今天我去了西湖，天气特别好，感觉特别开心
     */
    @GetMapping
    public Map<String, Object> write(@RequestParam("text") String inputText) {
        var resultFuture = compiledGraph.invoke(Map.of("original_text", inputText));
        var result = resultFuture.get();
        return result.data();
    }

}