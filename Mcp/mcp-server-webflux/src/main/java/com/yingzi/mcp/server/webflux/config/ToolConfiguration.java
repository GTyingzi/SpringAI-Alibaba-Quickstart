package com.yingzi.mcp.server.webflux.config;

import com.yingzi.mcp.server.webflux.component.baidutranslate.BaidutranslateProperties;
import com.yingzi.mcp.server.webflux.component.baidutranslate.BaidutranslateService;
import com.yingzi.mcp.server.webflux.component.time.TimeService;
import com.yingzi.mcp.server.webflux.component.weather.OpenMeteoService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties({BaidutranslateProperties.class})
@Configuration
public class ToolConfiguration {

    @Bean
    public ToolCallbackProvider weatherTools(OpenMeteoService openMeteoService, TimeService timeService, BaidutranslateService baidutranslateService) {
        return MethodToolCallbackProvider.builder().toolObjects(openMeteoService, timeService, baidutranslateService).build();
    }
}