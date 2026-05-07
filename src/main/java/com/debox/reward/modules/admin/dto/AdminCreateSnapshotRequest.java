package com.debox.reward.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminCreateSnapshotRequest {
    @NotBlank
    private String version;
    @NotBlank
    private String payloadJson;
}

