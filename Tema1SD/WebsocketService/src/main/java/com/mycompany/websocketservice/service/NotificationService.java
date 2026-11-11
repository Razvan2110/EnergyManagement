package com.mycompany.websocketservice.service;

import com.mycompany.websocketservice.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.OVERCONSUMPTION_QUEUE_NAME)
    public void handleOverconsumptionNotification(String message) {
        log.info("Received notification: {}", message);
        messagingTemplate.convertAndSend("/topic/notifications", message);
    }
}
