package com.debox.reward.modules.activity.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.activity.dto.CreateActivityOrderRequest;
import com.debox.reward.modules.activity.entity.ActivityIssue;
import com.debox.reward.modules.activity.entity.ActivityOrder;
import com.debox.reward.modules.activity.enums.ActivityIssueStatus;
import com.debox.reward.modules.activity.enums.ActivityOrderStatus;
import com.debox.reward.modules.activity.mapper.ActivityOrderMapper;
import com.debox.reward.modules.activity.service.ActivityIssueService;
import com.debox.reward.modules.activity.service.ActivityOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivityOrderServiceImpl extends ServiceImpl<ActivityOrderMapper, ActivityOrder> implements ActivityOrderService {

    private final ActivityIssueService activityIssueService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityOrder createOrder(CreateActivityOrderRequest request) {
        ActivityIssue issue = activityIssueService.getById(request.getIssueId());
        if (issue == null || issue.getStatus() != ActivityIssueStatus.OPEN) {
            throw new BizException("活动期号不可参与");
        }

        ActivityOrder order = new ActivityOrder();
        order.setOrderNo("AO" + System.currentTimeMillis());
        order.setUserId(request.getUserId());
        order.setIssueId(request.getIssueId());
        order.setAssetCode(request.getAssetCode());
        order.setAmount(request.getAmount());
        order.setStatus(ActivityOrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        save(order);
        return order;
    }
}
