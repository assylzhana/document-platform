package com.assylzhana.notification_service.service;

import com.assylzhana.notification_service.dto.Emails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;

    @KafkaListener(topics = "emails")
    public void handleDocExistenceResponse(Emails emails) {
        List<String> e = emails.getEmails();
        for(String email : e) {
            log.info(email);
            emailService.sendSimpleMessage(email, "Group","You were invited to the group. \n Group: "+emails.getGroupName()+"\n"+" Group members: " + emails.getEmails());
        }

    }
}
