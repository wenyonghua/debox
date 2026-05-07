package com.debox.reward.modules.admin.dto;

import lombok.Data;

@Data
public class AdminDrawRequest {
    /**
     * 可选：指定 seed；不传则随机
     */
    private Long seed;
    /**
     * 可选：中奖率 bp（默认 1000=10%）
     */
    private Integer winRateBp;
    /**
     * 可选：指定规则快照 ID；不传则用 payloadJson 创建
     */
    private Long ruleSnapshotId;
    /**
     * 可选：创建快照所需 json
     */
    private String payloadJson;
    /**
     * 可选：创建快照版本号
     */
    private String version;
}

