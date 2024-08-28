package com.assylzhana.collaboration_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Emails {
    List<String> emails;

    String groupName;
}
