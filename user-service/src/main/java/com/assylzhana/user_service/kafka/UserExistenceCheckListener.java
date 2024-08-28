package com.assylzhana.user_service.kafka;

import com.assylzhana.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserExistenceCheckListener {

    private final UserService userService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "check-user-existence-request")
    public void checkUserExistence(String email) {
        boolean exists = userService.existsByEmail(email);
        kafkaTemplate.send("check-user-existence-response", email + ":" + exists);
    }
}
