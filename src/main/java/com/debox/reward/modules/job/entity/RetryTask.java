package com.debox.reward.modules.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.debox.reward.modules.job.enums.RetryBizType;
import com.debox.reward.modules.job.enums.RetryTaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("retry_task")
public class RetryTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private RetryBizType bizType;

    /** 幂等唯一键（如 ORDER:123） */
    private String bizKey;

    private Long orderId;

    private Long issueId;

    /** 失败上下文 */
    private String lastError;

    private RetryTaskStatus status;

    private Integer retryCount;

    private Integer maxRetries;

    private LocalDateTime nextRetryAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
