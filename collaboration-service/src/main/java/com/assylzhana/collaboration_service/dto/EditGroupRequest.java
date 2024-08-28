package com.assylzhana.collaboration_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditGroupRequest {
    private List<String> emails;
    private List<String> documents;
}
