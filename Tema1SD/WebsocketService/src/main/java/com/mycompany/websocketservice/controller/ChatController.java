package com.mycompany.websocketservice.controller;

import com.mycompany.websocketservice.dto.ChatMessage;
import com.mycompany.websocketservice.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat")
    public void sendMessage(ChatMessage message) {
        chatService.processMessage(message);
    }

    @MessageMapping("/admin-reply")
    public void sendAdminReply(ChatMessage message) {
        chatService.sendAdminReply(message);
    }
}
