package com.assylzhana.user_service.dto;

import lombok.Data;

@Data
public class UserRequest {
    private String oldPassword;
    private String newPassword;
}
