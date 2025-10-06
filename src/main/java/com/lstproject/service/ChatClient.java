package com.lstproject.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

// 聊天室客户端
public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Scanner scanner;
    private boolean connected = false;

    public static void main(String[] args) {
        new ChatClient().startClient();
    }

    public void startClient() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            scanner = new Scanner(System.in);
            
            connected = true;
            
            // 启动接收消息的线程
            new Thread(this::receiveMessages).start();
            
            // 主线程处理用户输入
            handleUserInput();
            
        } catch (IOException e) {
            System.out.println("无法连接到服务器: " + e.getMessage());
        }
    }

    // 接收服务器消息
    private void receiveMessages() {
        try {
            String message;
            while (connected && (message = reader.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            if (connected) {
                System.out.println("与服务器连接断开");
            }
        }
    }

    // 处理用户输入
    private void handleUserInput() {
        try {
            String userInput;
            while (connected && scanner.hasNextLine()) {
                userInput = scanner.nextLine();
                writer.println(userInput);
                
                if (userInput.equals("/quit")) {
                    break;
                }
            }
        } finally {
            disconnect();
        }
    }

    // 断开连接
    private void disconnect() {
        connected = false;
        try {
            if (socket != null) socket.close();
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (scanner != null) scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
