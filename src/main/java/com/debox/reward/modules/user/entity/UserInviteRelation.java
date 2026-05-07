package com.debox.reward.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_invite_relation")
public class UserInviteRelation {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long parentId;
    private Integer depth;
    private String path;
    private LocalDateTime createdAt;
}
