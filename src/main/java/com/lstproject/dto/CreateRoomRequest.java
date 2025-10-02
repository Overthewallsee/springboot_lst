package com.lstproject.dto;

import javax.validation.constraints.NotBlank;

/**
 * 创建聊天室请求DTO
 * 用于接收创建聊天室时所需的参数
 */
public class CreateRoomRequest {
    
    /**
     * 聊天室ID
     */
    @NotBlank(message = "聊天室ID不能为空")
    private String roomId;
    
    /**
     * 聊天室密码
     */
    @NotBlank(message = "聊天室密码不能为空")
    private String password;
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    // Constructors
    public CreateRoomRequest() {}
    
    public CreateRoomRequest(String roomId, String password, String username) {
        this.roomId = roomId;
        this.password = password;
        this.username = username;
    }
    
    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}