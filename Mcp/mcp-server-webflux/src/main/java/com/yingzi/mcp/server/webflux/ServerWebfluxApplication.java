package com.yingzi.mcp.server.webflux;

import com.yingzi.mcp.server.webflux.config.ToolConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


/**
 * @author yingzi
 * @date 2025/3/28:10:21
 */
@SpringBootApplication
@Import(ToolConfiguration.class)
public class ServerWebfluxApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerWebfluxApplication.class, args);
    }
}
