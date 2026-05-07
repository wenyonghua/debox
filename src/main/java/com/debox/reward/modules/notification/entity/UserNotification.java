package com.debox.reward.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_notification")
public class UserNotification {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String body;
    private String type;
    private Integer readFlag;
    private LocalDateTime createdAt;
}
