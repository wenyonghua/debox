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
    private LocalDateTime settleTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
