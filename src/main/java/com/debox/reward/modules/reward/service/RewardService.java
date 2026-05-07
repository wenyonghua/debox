package com.debox.reward.modules.reward.service;

import com.debox.reward.modules.user.enums.UserRole;

import java.math.BigDecimal;

/**
 * 奖励发放服务
 */
public interface RewardService {

    /**
     * 根据奖励事件为指定用户发放奖励
     *
     * @param userId       被奖励的用户 ID
     * @param eventType    奖励事件类型（如 INVITE_REGISTER、ACTIVITY_PARTICIPATE）
     * @param sourceBizNo  触发奖励的业务单号（用于幂等）
     * @param userRole     用户角色（用于匹配规则）
     * @param baseAmount   基准金额（用于按比例计算奖励）
     */
    void grantReward(Long userId, String eventType, String sourceBizNo,
                     UserRole userRole, BigDecimal baseAmount);
}

