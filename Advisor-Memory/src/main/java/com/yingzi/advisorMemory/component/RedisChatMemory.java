package com.yingzi.advisorMemory.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author yingzi
 * @date 2025/3/23:16:25
 */
public class RedisChatMemory implements ChatMemory, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(com.alibaba.cloud.ai.memory.redis.RedisChatMemory.class);
    private static final String DEFAULT_KEY_PREFIX = "spring_ai_alibaba_chat_memory";
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 6379;
    private static final String DEFAULT_PASSWORD = null;
    private final JedisPool jedisPool;
    private final Jedis jedis;
    private final ObjectMapper objectMapper;

    public RedisChatMemory() {
        this("127.0.0.1", 6379, DEFAULT_PASSWORD);
    }

    public RedisChatMemory(String host, int port, String password) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        this.jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
        this.jedis = this.jedisPool.getResource();
        this.objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Message.class, new MessageDeserializer());
        this.objectMapper.registerModule(module);

        logger.info("Connected to Redis at {}:{}", host, port);
    }

    public void add(String conversationId, List<Message> messages) {
        String key = "spring_ai_alibaba_chat_memory:" + conversationId;
        Iterator var4 = messages.iterator();

        while(var4.hasNext()) {
            Message message = (Message)var4.next();

            try {
                String messageJson = this.objectMapper.writeValueAsString(message);
                this.jedis.rpush(key, new String[]{messageJson});
            } catch (JsonProcessingException var7) {
                throw new RuntimeException("Error serializing message", var7);
            }
        }

        logger.info("Added messages to conversationId: {}", conversationId);
    }

    public List<Message> get(String conversationId, int lastN) {
        String key = "spring_ai_alibaba_chat_memory:" + conversationId;
        List<String> messageStrings = this.jedis.lrange(key, (long)(-lastN), -1L);
        List<Message> messages = new ArrayList();
        Iterator var6 = messageStrings.iterator();

        while(var6.hasNext()) {
            String messageString = (String)var6.next();

            try {
                Message message = (Message)this.objectMapper.readValue(messageString, Message.class);
                messages.add(message);
            } catch (JsonProcessingException var9) {
                throw new RuntimeException("Error deserializing message", var9);
            }
        }

        logger.info("Retrieved {} messages for conversationId: {}", messages.size(), conversationId);
        return messages;
    }

    public void clear(String conversationId) {
        String key = "spring_ai_alibaba_chat_memory" + conversationId;
        this.jedis.del(key);
        logger.info("Cleared messages for conversationId: {}", conversationId);
    }

    public void close() {
        if (this.jedis != null) {
            this.jedis.close();
            logger.info("Redis connection closed.");
        }

        if (this.jedisPool != null) {
            this.jedisPool.close();
            logger.info("Jedis pool closed.");
        }

    }

    public void clearOverLimit(String conversationId, int maxLimit, int deleteSize) {
        try {
            String key = "spring_ai_alibaba_chat_memory" + conversationId;
            List<String> all = this.jedis.lrange(key, 0L, -1L);
            if (all.size() >= maxLimit) {
                all = all.stream().skip((long)Math.max(0, deleteSize)).toList();
            }

            this.clear(conversationId);
            Iterator var6 = all.iterator();

            while(var6.hasNext()) {
                String message = (String)var6.next();
                this.jedis.rpush(key, new String[]{message});
            }

        } catch (Exception var8) {
            logger.error("Error clearing messages from Redis chat memory", var8);
            throw new RuntimeException(var8);
        }
    }

    public void updateMessageById(String conversationId, String messages) {
        String key = "spring_ai_alibaba_chat_memory:" + conversationId;
        try {
            this.jedis.del(key);
            this.jedis.rpush(key, new String[]{messages});
        } catch (Exception var6) {
            logger.error("Error updating messages from Redis chat memory", var6);
            throw new RuntimeException(var6);
        }
    }
}

