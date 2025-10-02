package com.lstproject.service;

import com.lstproject.dto.ChatRoom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ChatRedisService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String CHAT_ROOM_PREFIX = "chat_room:";
    private static final String CHAT_ROOM_USERS_PREFIX = "chat_room_users:";
    
    /**
     * 保存聊天室信息到Redis
     * @param chatRoom 聊天室对象
     */
    public void saveChatRoom(ChatRoom chatRoom) {
        String key = CHAT_ROOM_PREFIX + chatRoom.getRoomId();
        redisTemplate.opsForValue().set(key, chatRoom.getPassword());
    }
    
    /**
     * 从Redis获取聊天室信息
     * @param roomId 聊天室ID
     * @return 聊天室对象，如果不存在返回null
     */
    public ChatRoom getChatRoom(String roomId) {
        String key = CHAT_ROOM_PREFIX + roomId;
        String password = redisTemplate.opsForValue().get(key);
        if (password != null) {
            return new ChatRoom(roomId, password);
        }
        return null;
    }
    
    /**
     * 验证聊天室密码
     * @param roomId 聊天室ID
     * @param password 输入的密码
     * @return 密码是否正确
     */
    public boolean verifyRoomPassword(String roomId, String password) {
        ChatRoom chatRoom = getChatRoom(roomId);
        if (chatRoom == null) {
            return true; // 如果聊天室不存在，允许创建
        }
        return chatRoom.verifyPassword(password);
    }
    
    /**
     * 检查聊天室是否存在
     * @param roomId 聊天室ID
     * @return 聊天室是否存在
     */
    public boolean isRoomExists(String roomId) {
        String key = CHAT_ROOM_PREFIX + roomId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 添加用户到聊天室用户列表
     * @param roomId 聊天室ID
     * @param username 用户名
     */
    public void addUserToRoom(String roomId, String username) {
        String key = CHAT_ROOM_USERS_PREFIX + roomId;
        redisTemplate.opsForSet().add(key, username);
    }
    
    /**
     * 从聊天室用户列表中移除用户
     * @param roomId 聊天室ID
     * @param username 用户名
     */
    public void removeUserFromRoom(String roomId, String username) {
        String key = CHAT_ROOM_USERS_PREFIX + roomId;
        redisTemplate.opsForSet().remove(key, username);
    }
    
    /**
     * 获取聊天室所有用户
     * @param roomId 聊天室ID
     * @return 用户名集合
     */
    public Set<String> getRoomUsers(String roomId) {
        String key = CHAT_ROOM_USERS_PREFIX + roomId;
        return redisTemplate.opsForSet().members(key);
    }
    
    /**
     * 检查用户是否已在聊天室中
     * @param roomId 聊天室ID
     * @param username 用户名
     * @return 用户是否已在聊天室中
     */
    public boolean isUserInRoom(String roomId, String username) {
        String key = CHAT_ROOM_USERS_PREFIX + roomId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, username));
    }
}