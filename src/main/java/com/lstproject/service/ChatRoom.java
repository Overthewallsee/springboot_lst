package com.lstproject.dto;

public class ChatRoom {
    private String roomId;
    private String password;
    
    public ChatRoom(String roomId, String password) {
        this.roomId = roomId;
        this.password = password;
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
    
    public boolean verifyPassword(String inputPassword) {
        return this.password != null && this.password.equals(inputPassword);
    }
}