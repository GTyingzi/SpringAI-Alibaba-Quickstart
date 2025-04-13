package com.yingzi.mcp.server.webflux.config;

import com.yingzi.mcp.server.webflux.component.baidutranslate.BaidutranslateProperties;
import com.yingzi.mcp.server.webflux.component.baidutranslate.BaidutranslateService;
import com.yingzi.mcp.server.webflux.component.time.TimeService;
import com.yingzi.mcp.server.webflux.component.weather.OpenMeteoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.tool.util.ToolUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

@EnableConfigurationProperties({BaidutranslateProperties.class})
@Configuration
public class ToolConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ToolConfiguration.class);


//    @Bean
//    public ToolCallbackProvider weatherTools(OpenMeteoService openMeteoService, TimeService timeService, BaidutranslateService baidutranslateService) {
//        return MethodToolCallbackProvider.builder().toolObjects(openMeteoService, timeService, baidutranslateService).build();
//    }

    @Bean
    public ToolCallbackProvider weatherTools(TimeService timeService) {
        MethodToolCallbackProvider.builder().build();
        return MethodToolCallbackProvider.builder().toolObjects(timeService).build();
    }

//    @Bean
//    public ToolCallback[]  weatherTools(TimeService timeService) {
//        Method cityTimeMethod = getCityTimeMethod();
//        return new MethodToolCallback[]{MethodToolCallback.builder().toolDefinition(ToolDefinition.from(cityTimeMethod)).toolMetadata(ToolMetadata.from(cityTimeMethod)).toolMethod(cityTimeMethod).toolObject(cityTimeMethod).toolCallResultConverter(ToolUtils.getToolCallResultConverter(cityTimeMethod)).build()};
//    }

    private Method getCityTimeMethod() {
        try {
            return TimeService.class.getMethod("getCityTimeMethod", String.class);
        } catch (NoSuchMethodException e) {
            logger.error("Failed to get method due to: {}", e.getMessage());
        }
        return null;
    }
}