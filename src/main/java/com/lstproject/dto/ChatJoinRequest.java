package com.lstproject.dto;

import javax.validation.constraints.NotBlank;

/**
 * 聊天室加入请求DTO
 * 用于接收用户加入聊天室时所需的参数
 */
public class ChatJoinRequest {
    
    /**
     * 聊天室ID
     */
    @NotBlank(message = "聊天室ID不能为空")
    private String roomId;
    
    /**
     * 聊天室密码
     */
    @NotBlank(message = "聊天室密码不能为空")
    private String roomPassword;
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    // Constructors
    public ChatJoinRequest() {}
    
    public ChatJoinRequest(String roomId, String roomPassword, String username) {
        this.roomId = roomId;
        this.roomPassword = roomPassword;
        this.username = username;
    }
    
    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public String getRoomPassword() {
        return roomPassword;
    }
    
    public void setRoomPassword(String roomPassword) {
        this.roomPassword = roomPassword;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}
