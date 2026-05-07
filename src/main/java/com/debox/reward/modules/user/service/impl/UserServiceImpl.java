package com.debox.reward.modules.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.user.dto.RegisterRequest;
import com.debox.reward.modules.user.entity.User;
import com.debox.reward.modules.user.entity.UserInviteRelation;
import com.debox.reward.modules.user.enums.UserRole;
import com.debox.reward.modules.user.mapper.UserInviteRelationMapper;
import com.debox.reward.modules.user.mapper.UserMapper;
import com.debox.reward.modules.user.service.UserService;
import com.debox.reward.modules.wallet.service.WalletAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final UserInviteRelationMapper relationMapper;
    private final WalletAccountService walletAccountService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(RegisterRequest request) {
        Long count = userMapper.selectCount(Wrappers.<User>lambdaQuery().eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BizException("用户名已存在");
        }

        User parent = null;
        if (request.getInviteCode() != null && !request.getInviteCode().isBlank()) {
            parent = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getInviteCode, request.getInviteCode()));
            if (parent == null) {
                throw new BizException("邀请码不存在");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUserNo("U" + System.currentTimeMillis());
        user.setUsername(request.getUsername());
        user.setMobile(request.getMobile());
        user.setPasswordHash(request.getPassword());
        user.setInviteCode(UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        user.setParentId(parent == null ? null : parent.getId());
        user.setRole(UserRole.MEMBER);
        user.setStatus(1);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);

        UserInviteRelation relation = new UserInviteRelation();
        relation.setUserId(user.getId());
        relation.setParentId(user.getParentId());
        relation.setDepth(parent == null ? 0 : 1);
        relation.setPath(parent == null ? String.valueOf(user.getId()) : parent.getId() + "/" + user.getId());
        relation.setCreatedAt(now);
        relationMapper.insert(relation);

        walletAccountService.createDefaultAccounts(user.getId());
        return user;
    }
}
