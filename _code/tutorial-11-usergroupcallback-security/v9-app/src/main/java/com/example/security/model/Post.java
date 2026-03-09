package com.example.security.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for JSONPlaceholder Post response
 */
public class Post {
    
    @JsonProperty("userId")
    private Integer userId;
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("body")
    private String body;
    
    public Post() {
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    @Override
    public String toString() {
        return "Post{" +
                "userId=" + userId +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}

