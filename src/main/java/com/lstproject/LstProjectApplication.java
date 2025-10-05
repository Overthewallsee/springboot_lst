package com.lstproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class LstProjectApplication {

    
    public static void main(String[] args) {
        // 确保控制台输出使用UTF-8编码
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");
        
        // 设置控制台输出编码
        if (System.console() != null) {
            System.setOut(new java.io.PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new java.io.PrintStream(System.err, true, StandardCharsets.UTF_8));
        }
        
        SpringApplication.run(LstProjectApplication.class, args);
    }
}