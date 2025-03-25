package com.yingzi.toolCalling.component.baidutranslate;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yingzi
 * @date 2025/3/24:19:27
 */
@ConfigurationProperties(prefix = "spring.ai.toolcalling.baidutranslate")
public class BaidutranslateProperties {

    private String appId;

    private String secretKey;

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

}
