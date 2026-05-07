package com.debox.reward.modules.reward.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rule_snapshot")
public class RuleSnapshot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String version;
    private String payloadJson;
    private LocalDateTime createdAt;
}

