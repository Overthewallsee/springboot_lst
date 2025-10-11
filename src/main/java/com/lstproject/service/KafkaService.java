package com.lstproject.service;


import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaService {

    private static final String CHAT_TOPIC = "chat-messages";

    private static KafkaTemplate<String, String> staticKafkaTemplate;
    public static void sendChatMessage(String message, String roomId) {
        // 将消息发送到Kafka主题
        String kafkaMessage = roomId + " |=| " + message;
        if (staticKafkaTemplate != null) {
            staticKafkaTemplate.send(CHAT_TOPIC, kafkaMessage);
        } else {
            System.err.println("KafkaTemplate is not initialized");
        }
    }
}
