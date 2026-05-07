package com.debox.reward.modules.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.debox.reward.modules.activity.dto.CreateActivityOrderRequest;
import com.debox.reward.modules.activity.entity.ActivityOrder;

public interface ActivityOrderService extends IService<ActivityOrder> {

    ActivityOrder createOrder(CreateActivityOrderRequest request);

    /**
     * 结算指定期号下所有未结算订单
     *
     * @param issueId 期号 ID
     */
    void settleIssueOrders(Long issueId);

    /**
     * 单笔订单结算（独立事务，可重试；已结算则直接返回）
     */
    void settleSingleOrder(Long orderId);
}
