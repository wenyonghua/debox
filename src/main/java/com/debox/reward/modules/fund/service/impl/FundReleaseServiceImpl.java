package com.debox.reward.modules.fund.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.debox.reward.modules.fund.entity.FundReleaseEvent;
import com.debox.reward.modules.fund.entity.FundReleasePlan;
import com.debox.reward.modules.fund.mapper.FundReleaseEventMapper;
import com.debox.reward.modules.fund.mapper.FundReleasePlanMapper;
import com.debox.reward.modules.fund.service.FundReleaseService;
import com.debox.reward.modules.wallet.enums.AssetCode;
import com.debox.reward.modules.wallet.enums.WalletBizType;
import com.debox.reward.modules.wallet.service.WalletLedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundReleaseServiceImpl implements FundReleaseService {

    private static final BigDecimal DEFAULT_DAILY_RATE = new BigDecimal("0.005"); // 0.5%

    private final FundReleasePlanMapper planMapper;
    private final FundReleaseEventMapper eventMapper;
    private final WalletLedgerService walletLedgerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPlan(Long userId, String sourceBizNo, BigDecimal amount) {
        FundReleasePlan plan = new FundReleasePlan();
        plan.setUserId(userId);
        plan.setSourceBizNo(sourceBizNo);
        plan.setTotalAmount(amount);
        plan.setRemainingAmount(amount);
        plan.setDailyRate(DEFAULT_DAILY_RATE);
        plan.setStatus(1);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        planMapper.insert(plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void runDailyRelease() {
        List<FundReleasePlan> plans = planMapper.selectList(Wrappers.<FundReleasePlan>lambdaQuery()
                .eq(FundReleasePlan::getStatus, 1)
                .gt(FundReleasePlan::getRemainingAmount, BigDecimal.ZERO));
        if (plans.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();
        for (FundReleasePlan plan : plans) {
            // 幂等：同 plan + date 只生成一次
            Long exists = eventMapper.selectCount(Wrappers.<FundReleaseEvent>lambdaQuery()
                    .eq(FundReleaseEvent::getPlanId, plan.getId())
                    .eq(FundReleaseEvent::getReleaseDate, today));
            if (exists != null && exists > 0) {
                continue;
            }

            BigDecimal amt = plan.getRemainingAmount().multiply(plan.getDailyRate())
                    .setScale(18, RoundingMode.DOWN);
            if (amt.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            if (amt.compareTo(plan.getRemainingAmount()) > 0) {
                amt = plan.getRemainingAmount();
            }

            String bizNo = "FRE-" + plan.getId() + "-" + today;
            FundReleaseEvent ev = new FundReleaseEvent();
            ev.setPlanId(plan.getId());
            ev.setUserId(plan.getUserId());
            ev.setReleaseDate(today);
            ev.setAmount(amt);
            ev.setStatus(0);
            ev.setBizNo(bizNo);
            ev.setCreatedAt(LocalDateTime.now());
            eventMapper.insert(ev);

            try {
                // 从待释放账户扣除，释放到 SIX 可用余额
                walletLedgerService.debit(plan.getUserId(), AssetCode.FUND_SIX, amt,
                        WalletBizType.FUND_RELEASE, bizNo + "-D", "基金释放扣减-" + bizNo);
                walletLedgerService.credit(plan.getUserId(), AssetCode.SIX, amt,
                        WalletBizType.FUND_RELEASE, bizNo + "-C", "基金释放到账-" + bizNo);

                plan.setRemainingAmount(plan.getRemainingAmount().subtract(amt));
                plan.setUpdatedAt(LocalDateTime.now());
                if (plan.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    plan.setStatus(2);
                }
                planMapper.updateById(plan);

                ev.setStatus(1);
                ev.setPostedAt(LocalDateTime.now());
                eventMapper.updateById(ev);
            } catch (Exception e) {
                ev.setStatus(2);
                eventMapper.updateById(ev);
                log.error("基金释放失败: planId={}, bizNo={}", plan.getId(), bizNo, e);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryFailedEvents(Long planId, int limit) {
        int l = Math.max(1, Math.min(200, limit));
        List<FundReleaseEvent> list = eventMapper.selectList(Wrappers.<FundReleaseEvent>lambdaQuery()
                .eq(planId != null, FundReleaseEvent::getPlanId, planId)
                .eq(FundReleaseEvent::getStatus, 2)
                .orderByAsc(FundReleaseEvent::getId)
                .last("limit " + l));
        for (FundReleaseEvent ev : list) {
            try {
                // 幂等依赖 wallet_ledger.biz_no 唯一键
                walletLedgerService.debit(ev.getUserId(), AssetCode.FUND_SIX, ev.getAmount(),
                        WalletBizType.FUND_RELEASE, ev.getBizNo() + "-D", "基金释放重试扣减-" + ev.getBizNo());
                walletLedgerService.credit(ev.getUserId(), AssetCode.SIX, ev.getAmount(),
                        WalletBizType.FUND_RELEASE, ev.getBizNo() + "-C", "基金释放重试到账-" + ev.getBizNo());

                ev.setStatus(1);
                ev.setPostedAt(LocalDateTime.now());
                eventMapper.updateById(ev);
            } catch (Exception e) {
                log.error("基金释放重试失败: eventId={}, bizNo={}", ev.getId(), ev.getBizNo(), e);
            }
        }
    }
}

