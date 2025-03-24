package com.yingzi.advisorMemory.component;

/**
 * @author yingzi
 * @date 2025/3/23:11:19
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

public class MysqlChatMemory implements ChatMemory, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(com.alibaba.cloud.ai.memory.mysql.MysqlChatMemory.class);
    private static final String DEFAULT_DATABASE = "spring_ai_alibaba_mysql";
    private static final String DEFAULT_TABLE_NAME = "chat_memory";
    private static final String DEFAULT_URL = "127.0.0.1:3306";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "root";
    private final Connection connection;
    private final ObjectMapper objectMapper;

    public MysqlChatMemory() {
        this("root", "root", "127.0.0.1:3306");
    }

    public MysqlChatMemory(String username, String password, String url) {
        this.objectMapper = new ObjectMapper();
        // 配置ObjectMapper以支持接口反序列化
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Message.class, new MessageDeserializer());
        this.objectMapper.registerModule(module);

        try {
            this.connection = DriverManager.getConnection(String.format("jdbc:mysql://%s/%s?serverTimezone=UTC", url, "spring_ai_alibaba_mysql"), username, password);
            this.checkAndCreateTable();
        } catch (SQLException var5) {
            throw new RuntimeException("Error connecting to the database", var5);
        }
    }

    public MysqlChatMemory(Connection connection) {
        this.objectMapper = new ObjectMapper();
        // 配置ObjectMapper以支持接口反序列化
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Message.class, new MessageDeserializer());
        this.objectMapper.registerModule(module);

        this.connection = connection;

        try {
            this.checkAndCreateTable();
        } catch (SQLException var3) {
            throw new RuntimeException("Error checking the database table", var3);
        }
    }

    private void checkAndCreateTable() throws SQLException {
        String checkTableQuery = String.format("SHOW TABLES LIKE '%s'", "chat_memory");
        Statement stmt = this.connection.createStatement();

        try {
            ResultSet rs = stmt.executeQuery(checkTableQuery);

            try {
                if (rs.next()) {
                    logger.info("Table chat_memory exists.");
                } else {
                    logger.info("Table chat_memory does not exist. Creating table...");
                    this.createTable();
                }
            } catch (Throwable var8) {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                    }
                }

                throw var8;
            }

            if (rs != null) {
                rs.close();
            }
        } catch (Throwable var9) {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Throwable var6) {
                    var9.addSuppressed(var6);
                }
            }

            throw var9;
        }

        if (stmt != null) {
            stmt.close();
        }

    }

    private void createTable() {
        try {
            Statement stmt = this.connection.createStatement();

            try {
                stmt.execute("USE spring_ai_alibaba_mysql");
                stmt.execute("CREATE TABLE chat_memory( id BIGINT AUTO_INCREMENT PRIMARY KEY,conversation_id  VARBINARY(256)  NULL,messages TEXT NULL,UNIQUE (conversation_id));");
                logger.info("Table chat_memory created successfully.");
            } catch (Throwable var5) {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (Throwable var4) {
                        var5.addSuppressed(var4);
                    }
                }

                throw var5;
            }

            if (stmt != null) {
                stmt.close();
            }

        } catch (Exception var6) {
            throw new RuntimeException("Error creating table chat_memory ", var6);
        }
    }

    public void add(String conversationId, List<Message> messages) {
        try {
            List<Message> all = this.selectMessageById(conversationId);
            all.addAll(messages);
            this.updateMessageById(conversationId, this.objectMapper.writeValueAsString(all));
        } catch (Exception var4) {
            logger.error("Error adding messages to MySQL chat memory", var4);
            throw new RuntimeException(var4);
        }
    }

    public List<Message> get(String conversationId, int lastN) {
        List all;
        try {
            all = this.selectMessageById(conversationId);
        } catch (Exception var5) {
            logger.error("Error getting messages from MySQL chat memory", var5);
            throw new RuntimeException(var5);
        }

        return all != null ? all.stream().skip((long) Math.max(0, all.size() - lastN)).toList() : List.of();
    }

    public void clear(String conversationId) {
        StringBuilder sql = new StringBuilder("DELETE FROM chat_memory WHERE conversation_id = '");
        sql.append(conversationId);
        sql.append("'");

        try {
            Statement stmt = this.connection.createStatement();

            try {
                stmt.executeUpdate(sql.toString());
            } catch (Throwable var7) {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                    }
                }

                throw var7;
            }

            if (stmt != null) {
                stmt.close();
            }

        } catch (Exception var8) {
            throw new RuntimeException("Error executing delete ", var8);
        }
    }

    public void close() throws Exception {
        if (this.connection != null) {
            this.connection.close();
        }

    }

    public void clearOverLimit(String conversationId, int maxLimit, int deleteSize) {
        try {
            List<Message> all = this.selectMessageById(conversationId);
            if (all.size() >= maxLimit) {
                all = all.stream().skip((long) Math.max(0, deleteSize)).toList();
                this.updateMessageById(conversationId, this.objectMapper.writeValueAsString(all));
            }

        } catch (Exception var5) {
            logger.error("Error clearing messages from MySQL chat memory", var5);
            throw new RuntimeException(var5);
        }
    }

    public List<Message> selectMessageById(String conversationId) {
        List<Message> totalMessage = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT messages FROM chat_memory WHERE conversation_id = '");
        sql.append(conversationId);
        sql.append("'");

        try {
            Statement stmt = this.connection.createStatement();

            try {
                ResultSet resultSet = stmt.executeQuery(sql.toString());

                while (resultSet.next()) {
                    String oldMessage = resultSet.getString("messages");
//                    oldMessage = filterIllegalCharacter(oldMessage);
                    if (oldMessage != null && !oldMessage.isEmpty()) {
                        List<Message> messages = this.objectMapper.readValue(oldMessage, new TypeReference<>() {
                        });
                        totalMessage.addAll(messages);
                    }
                }
            } catch (Throwable var9) {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                    }
                }

                throw var9;
            }

            if (stmt != null) {
                stmt.close();
            }

            return totalMessage;
        } catch (SQLException | JsonProcessingException var10) {
            logger.error("select message by mysql error，sql:{}", sql, var10);
            throw new RuntimeException(var10);
        }
    }

    public void updateMessageById(String conversationId, String messages) {
        // Remove newlines and escape single quotes
        messages = messages.replaceAll("[\\r\\n]", "").replace("'", "''");

        String sql;
        if (this.selectMessageById(conversationId).isEmpty()) {
            sql = "INSERT INTO chat_memory (messages, conversation_id) VALUES (?, ?)";
        } else {
            sql = "UPDATE chat_memory SET messages = ? WHERE conversation_id = ?";
        }

        try (PreparedStatement stmt = this.connection.prepareStatement(sql)) {
            if (this.selectMessageById(conversationId).isEmpty()) {
                stmt.setString(1, messages);
                stmt.setString(2, conversationId);
            } else {
                stmt.setString(1, messages);
                stmt.setString(2, conversationId);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("update message by mysql error，sql:{}", sql, e);
            throw new RuntimeException(e);
        }
    }


}
