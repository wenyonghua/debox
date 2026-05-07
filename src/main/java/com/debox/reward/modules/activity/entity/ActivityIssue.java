package com.debox.reward.modules.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.debox.reward.modules.activity.enums.ActivityIssueStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("activity_issue")
public class ActivityIssue {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String issueNo;
    private String title;
    private ActivityIssueStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /**
     * 开奖结果负载（JSON 字符串）：包含 seed、winRateBp、算法版本等
     */
    private String resultPayload;
    /**
     * 开奖时间
     */
    private LocalDateTime drawnAt;
    /**
     * 规则快照 ID（占位：后续接 rule_snapshot）
     */
    private Long ruleSnapshotId;
    private LocalDateTime settleTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
