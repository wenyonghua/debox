package com.debox.reward.modules.reward.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.debox.reward.modules.reward.entity.RewardRecord;
import com.debox.reward.modules.reward.entity.RewardRule;
import com.debox.reward.modules.reward.mapper.RewardRecordMapper;
import com.debox.reward.modules.reward.service.RewardRuleService;
import com.debox.reward.modules.reward.service.RewardService;
import com.debox.reward.modules.user.enums.UserRole;
import com.debox.reward.modules.wallet.enums.WalletBizType;
import com.debox.reward.modules.wallet.service.WalletLedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardServiceImpl implements RewardService {

    private final RewardRuleService rewardRuleService;
    private final RewardRecordMapper rewardRecordMapper;
    private final WalletLedgerService walletLedgerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantReward(Long userId, String eventType, String sourceBizNo,
                            UserRole userRole, BigDecimal baseAmount) {
        // 查询启用中且有效期内的规则
        LocalDateTime now = LocalDateTime.now();
        List<RewardRule> rules = rewardRuleService.list(
                Wrappers.<RewardRule>lambdaQuery()
                        .eq(RewardRule::getEventType, eventType)
                        .eq(RewardRule::getUserRole, userRole)
                        .eq(RewardRule::getEnabled, 1)
                        .and(w -> w.isNull(RewardRule::getEffectiveAt)
                                .or().le(RewardRule::getEffectiveAt, now))
                        .and(w -> w.isNull(RewardRule::getExpiredAt)
                                .or().ge(RewardRule::getExpiredAt, now))
        );

        if (rules.isEmpty()) {
            log.debug("未找到匹配奖励规则: userId={}, eventType={}, role={}", userId, eventType, userRole);
            return;
        }

        for (RewardRule rule : rules) {
            String rewardNo = "RW" + System.currentTimeMillis() + userId;
            // 幂等检查：同一来源业务单号 + 事件类型已发放则跳过
            Long exists = rewardRecordMapper.selectCount(
                    Wrappers.<RewardRecord>lambdaQuery()
                            .eq(RewardRecord::getSourceBizNo, sourceBizNo)
                            .eq(RewardRecord::getEventType, eventType)
                            .eq(RewardRecord::getUserId, userId)
            );
            if (exists > 0) {
                log.warn("奖励已发放（幂等）: userId={}, sourceBizNo={}", userId, sourceBizNo);
                continue;
            }

            // 计算奖励金额
            BigDecimal rewardAmount = baseAmount.multiply(rule.getRewardRate())
                    .setScale(18, RoundingMode.DOWN);
            if (rule.getMaxRewardAmount() != null
                    && rewardAmount.compareTo(rule.getMaxRewardAmount()) > 0) {
                rewardAmount = rule.getMaxRewardAmount();
            }
            if (rewardAmount.compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("奖励金额为零，跳过: ruleId={}", rule.getId());
                continue;
            }

            // 写入奖励记录
            LocalDateTime now2 = LocalDateTime.now();
            RewardRecord record = new RewardRecord();
            record.setRewardNo(rewardNo);
            record.setUserId(userId);
            record.setEventType(eventType);
            record.setSourceBizNo(sourceBizNo);
            record.setAssetCode(rule.getRewardAssetCode());
            record.setAmount(rewardAmount);
            record.setStatus(1); // 1=已发放
            record.setRemark("奖励规则ID:" + rule.getId());
            record.setCreatedAt(now2);
            record.setGrantedAt(now2);
            rewardRecordMapper.insert(record);

            // 入账到钱包
            WalletBizType bizType = resolveBizType(eventType);
            walletLedgerService.credit(userId, rule.getRewardAssetCode(), rewardAmount,
                    bizType, rewardNo, "奖励发放-" + eventType);

            log.info("奖励发放成功: userId={}, rewardNo={}, amount={}, asset={}",
                    userId, rewardNo, rewardAmount, rule.getRewardAssetCode());
        }
    }

    private WalletBizType resolveBizType(String eventType) {
        if (eventType == null) {
            return WalletBizType.ACTIVITY_REWARD;
        }
        if (eventType.startsWith("INVITE_")) {
            return WalletBizType.INVITE_REWARD;
        }
        return WalletBizType.ACTIVITY_REWARD;
    }
}

