package com.lstproject.service;

import com.lstproject.dto.ChatMessage;
import com.lstproject.dto.LoginRequest;
import com.lstproject.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatService {

    @Value("${socket.port:8889}")
    private int PORT;
    public void sendMsg(ChatMessage chatMessage) throws IOException {
        String message = chatMessage.getMessage();
        String sender = chatMessage.getSender();
        String roomId = chatMessage.getRoomId();
        ClientHandler clientHandler = null;
        if (!ChatServer.chatRooms.containsKey(roomId)) {
            clientHandler = addClient(sender, roomId);
        } else {
            Map<String, ClientHandler> chatMap = ChatServer.chatRooms.get(roomId);
            if (!chatMap.containsKey(sender)) {
                clientHandler = addClient(sender, roomId);
            }
        }
        clientHandler.sendMessage(message);
    }

    private ClientHandler addClient(String sender, String roomId) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Socket clientSocket = serverSocket.accept();
        ClientHandler clientHandler = new ClientHandler(clientSocket);
        ChatServer.addClient(roomId, sender, clientHandler);
        return clientHandler;
    }

}
