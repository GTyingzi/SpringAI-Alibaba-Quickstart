package com.yingzi.toolCalling.component.time;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

/**
 * @author yingzi
 * @date 2025/3/25:14:58
 */
@Configuration
@ConditionalOnClass({GetCurrentTimeByTimeZoneIdService.class})
@ConditionalOnProperty(prefix = "spring.ai.toolcalling.time", name = "enabled", havingValue = "true")
public class TimeAutoConfiguration {

    @Bean(name = "getCityTimeFunction")
    @ConditionalOnMissingBean
    @Description("Get the time of a specified city.")
    public GetCurrentTimeByTimeZoneIdService getCityTimeFunction() {
        return new GetCurrentTimeByTimeZoneIdService();
    }

}
