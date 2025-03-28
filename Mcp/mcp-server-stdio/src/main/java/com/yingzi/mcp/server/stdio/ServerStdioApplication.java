package com.yingzi.mcp.server.stdio;

import com.yingzi.mcp.server.stdio.config.ToolConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * @author yingzi
 * @date 2025/3/27:13:25
 */
@SpringBootApplication
@Import(ToolConfiguration.class)
public class ServerStdioApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerStdioApplication.class, args);
    }

}