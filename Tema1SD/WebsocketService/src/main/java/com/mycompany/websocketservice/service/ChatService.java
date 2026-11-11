package com.mycompany.websocketservice.service;

import com.mycompany.websocketservice.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;

    public void processMessage(ChatMessage message) {
        String responseContent = getResponse(message.getContent().toLowerCase());

        if (responseContent != null) {
            // Rule matched - Send to User
            ChatMessage response = new ChatMessage("System", responseContent, System.currentTimeMillis(), message.getSender());
            messagingTemplate.convertAndSend("/topic/user/" + message.getSender(), response);
        } else {
            
            messagingTemplate.convertAndSend("/topic/admin", message);
        }
    }

    private String getResponse(String content) {
        if (content.contains("hello") || content.contains("hi")) {
            return "Hello! How can I help you regarding the Energy Management System?";
        } else if (content.contains("consumption")) {
            return "You can view your real-time and historical energy consumption on the dashboard charts.";
        } else if (content.contains("device")) {
            return "Devices can be managed (added, updated, deleted) in the Device Management tab by administrators.";
        } else if (content.contains("alert") || content.contains("red")) {
            return "A red alert notification indicates that a device has exceeded its hourly consumption limit.";
        } else if (content.contains("admin")) {
            return "Administrators have full access to manage users, devices, and view system logs.";
        } else if (content.contains("price") || content.contains("cost")) {
            return "The current energy price is calculated based on standard hourly rates. Check your contract for details.";
        } else if (content.contains("hours") || content.contains("time")) {
            return "The system operates 24/7 to monitor your energy consumption continuously.";
        } else if (content.contains("contact") || content.contains("support")) {
            return "You can reach our human support team at support@ems.com or call +1-555-0199.";
        } else if (content.contains("login") || content.contains("password")) {
            return "If you are having trouble logging in, please try resetting your password or contact an admin.";
        } else if (content.contains("register") || content.contains("signup")) {
            return "New user accounts must be created by an administrator.";
        } else {
            return null; // No rule matched
        }
    }

    public void sendAdminReply(ChatMessage message) {
        // Admin replying to a user
        messagingTemplate.convertAndSend("/topic/user/" + message.getRecipient(), message);
    }
}
