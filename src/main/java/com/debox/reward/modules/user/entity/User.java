package com.debox.reward.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.debox.reward.modules.user.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String userNo;
    private String username;
    private String mobile;
    private String passwordHash;
    private String inviteCode;
    private Long parentId;
    private UserRole role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
