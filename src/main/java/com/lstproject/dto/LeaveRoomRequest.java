package com.lstproject.dto;

import javax.validation.constraints.NotBlank;

/**
 * 离开聊天室请求DTO
 * 用于接收用户离开聊天室时所需的参数
 */
public class LeaveRoomRequest {
    
    /**
     * 聊天室ID
     */
    @NotBlank(message = "聊天室ID不能为空")
    private String roomId;
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    // Constructors
    public LeaveRoomRequest() {}
    
    public LeaveRoomRequest(String roomId, String username) {
        this.roomId = roomId;
        this.username = username;
    }
    
    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}