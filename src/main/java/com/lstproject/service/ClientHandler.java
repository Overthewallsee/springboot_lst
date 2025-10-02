package com.lstproject.service;

import java.io.*;
import java.net.*;
import java.util.*;

// 处理每个客户端连接的类
class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String clientName;

    private String roomId;
    private boolean authenticated = false;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // 用户认证
            authenticateUser();
            
            if (authenticated) {
                handleChat();
            }
        } catch (IOException e) {
            System.out.println("客户端连接异常: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    // 用户认证
    private void authenticateUser() throws IOException {
        sendMessage("欢迎来到聊天室！请输入聊天室ID:");
        this.roomId = reader.readLine();
        
        if (roomId != null && !roomId.trim().isEmpty()) {
            // 检查聊天室是否存在，如果不存在则提示输入密码创建
            if (!ChatServer.isRoomExists(roomId)) {
                sendMessage("聊天室不存在，请输入密码创建聊天室:");
                String roomPassword = reader.readLine();
                if (ChatServer.createChatRoom(roomId, roomPassword)) {
                    sendMessage("聊天室创建成功！");
                } else {
                    sendMessage("聊天室创建失败，可能已存在同名聊天室");
                    return;
                }
            } else {
                // 聊天室存在，需要验证密码
                sendMessage("请输入聊天室密码:");
                String inputPassword = reader.readLine();
                
                if (!ChatServer.verifyRoomPassword(roomId, inputPassword)) {
                    sendMessage("密码错误，无法加入聊天室");
                    return;
                }
            }
            
            sendMessage("请输入您的昵称:");
            this.clientName = reader.readLine();
            
            if (clientName != null && !clientName.trim().isEmpty()) {
                // 检查用户是否已在聊天室中
                if (!ChatServer.staticChatRedisService.isUserInRoom(roomId, clientName)) {
                    authenticated = true;
                    ChatServer.addClient(roomId, clientName, this);
                    sendMessage("登录成功！输入 /help 查看帮助，输入 /quit 退出聊天室");
                } else {
                    sendMessage("该昵称已在聊天室中，请选择其他昵称");
                }
            }
        }
    }

    // 处理聊天消息
    private void handleChat() throws IOException {
        String message;
        while ((message = reader.readLine()) != null) {
            if (message.startsWith("/")) {
                handleCommand(message);
            } else {
                // 普通聊天消息
                ChatServer.broadcastMessage(roomId + " := " + clientName + ": " + message, clientName);
            }
        }
    }

    // 处理命令
    private void handleCommand(String command) {
        if (command.equals("/quit")) {
            sendMessage("再见！");
        } else if (command.equals("/users")) {
            sendMessage(ChatServer.getOnlineUsers(roomId));
        } else if (command.equals("/help")) {
            sendMessage("可用命令:");
            sendMessage("/users - 查看在线用户");
            sendMessage("/quit - 退出聊天室");
            sendMessage("/help - 显示帮助信息");
        } else {
            sendMessage("未知命令，输入 /help 查看帮助");
        }
    }

    // 发送消息给客户端
    public void sendMessage(String message) {
        writer.println(message);
    }

    // 清理资源
    private void cleanup() {
        try {
            if (authenticated) {
                ChatServer.removeClient(roomId, clientName);
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getClientName() {
        return clientName;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }
}