package com.yingzi.structedOutput.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author yingzi
 * @date 2025/4/2:18:05
 */
@JsonPropertyOrder({"title", "date", "author", "content"}) // 指定属性的顺序
public class BeanEntity {

    private String title;
    private String author;
    private String date;
    private String content;

    public BeanEntity() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "StreamToBeanEntity{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", date='" + date + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
