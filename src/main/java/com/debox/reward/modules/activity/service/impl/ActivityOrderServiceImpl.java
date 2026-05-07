package com.debox.reward.modules.activity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import com.debox.reward.modules.reward.entity.RewardAllocation;
import com.debox.reward.modules.reward.entity.RuleSnapshot;
import com.debox.reward.modules.reward.enums.AllocationType;
import com.debox.reward.modules.reward.service.RewardAllocationService;
import com.debox.reward.modules.reward.service.RewardService;
import com.debox.reward.modules.reward.service.RuleSnapshotService;
import com.debox.reward.modules.fund.service.FundReleaseService;
import com.debox.reward.modules.user.entity.User;
import com.debox.reward.modules.user.mapper.UserMapper;
import com.debox.reward.modules.wallet.enums.AssetCode;
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
public class ActivityOrderServiceImpl extends ServiceImpl<ActivityOrderMapper, ActivityOrder>
        implements ActivityOrderService {

    private final ActivityIssueService activityIssueService;
    private final WalletLedgerService walletLedgerService;
    private final RewardService rewardService;
    private final RewardAllocationService rewardAllocationService;
    private final RuleSnapshotService ruleSnapshotService;
    private final FundReleaseService fundReleaseService;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityOrder createOrder(CreateActivityOrderRequest request) {
        ActivityIssue issue = activityIssueService.getById(request.getIssueId());
        if (issue == null || issue.getStatus() != ActivityIssueStatus.OPEN) {
            throw new BizException("活动期号不可参与");
        }

        String orderNo = "AO" + System.currentTimeMillis() + request.getUserId();
        ActivityOrder order = new ActivityOrder();
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setIssueId(request.getIssueId());
        order.setAssetCode(request.getAssetCode());
        order.setAmount(request.getAmount());
        order.setStatus(ActivityOrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        save(order);

        // 冻结用户积分
        walletLedgerService.freeze(request.getUserId(), request.getAssetCode(),
                request.getAmount(), "FREEZE-" + orderNo, "参与活动冻结-" + issue.getIssueNo());

        log.info("活动订单创建并冻结: orderNo={}, userId={}, amount={}",
                orderNo, request.getUserId(), request.getAmount());
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleIssueOrders(Long issueId) {
        ActivityIssue issue = activityIssueService.getById(issueId);
        if (issue == null) {
            throw new BizException("期号不存在: " + issueId);
        }
        if (issue.getDrawnAt() == null || issue.getResultPayload() == null || issue.getResultPayload().isBlank()) {
            throw new BizException("期号尚未开奖，禁止结算: " + issue.getIssueNo());
        }

        Long snapshotId = issue.getRuleSnapshotId();
        if (snapshotId == null) {
            snapshotId = parseLongObj(issue.getResultPayload(), "\"ruleSnapshotId\":");
        }
        RuleSnapshot snapshot = snapshotId == null ? null : ruleSnapshotService.getById(snapshotId);
        String rules = snapshot == null ? null : snapshot.getPayloadJson();

        List<ActivityOrder> orders = list(Wrappers.<ActivityOrder>lambdaQuery()
                .eq(ActivityOrder::getIssueId, issueId)
                .eq(ActivityOrder::getStatus, ActivityOrderStatus.CREATED));

        for (ActivityOrder order : orders) {
            // 解冻积分
            walletLedgerService.unfreeze(order.getUserId(), order.getAssetCode(),
                    order.getAmount(),
                    "UNFREEZE-" + order.getOrderNo(),
                    "活动结算解冻-" + order.getOrderNo());

            // 扣除参与本金（从可用余额扣掉）
            walletLedgerService.debit(order.getUserId(), order.getAssetCode(), order.getAmount(),
                    WalletBizType.ORDER_STAKE, "STAKE-" + order.getOrderNo(), "活动参与扣除本金-" + order.getOrderNo());

            // 判定中奖/不中奖（按期次 seed + winRateBp）
            long seed = parseLong(issue.getResultPayload(), "\"seed\":", 0L);
            int winRateBp = (int) parseLong(issue.getResultPayload(), "\"winRateBp\":",
                    rules == null ? 1000L : parseLong(rules, "\"winRateBp\":", 1000L));
            boolean isWin = isWinBySeed(order.getOrderNo(), seed, winRateBp);

            // 查询用户角色，触发分润/返水事件（由 reward_rule 配置驱动）
            User user = userMapper.selectById(order.getUserId());
            if (user != null) {
                rewardService.grantReward(order.getUserId(),
                        isWin ? "ACTIVITY_WIN_SELF" : "ACTIVITY_LOSE_SELF",
                        order.getOrderNo(), user.getRole(), order.getAmount());

                // 如果用户有上级，触发上级的分润/返水事件
                if (user.getParentId() != null) {
                    User parent = userMapper.selectById(user.getParentId());
                    if (parent != null) {
                        rewardService.grantReward(parent.getId(),
                                isWin ? "ACTIVITY_WIN_UPLINE" : "ACTIVITY_LOSE_UPLINE",
                                order.getOrderNo(), parent.getRole(), order.getAmount());
                    }
                }
            }

            if (isWin) {
                BigDecimal multiplier = parseDecimal(rules, "\"multiplier\":\"", new BigDecimal("47"));
                BigDecimal winFeeRate = parseDecimal(rules, "\"winFeeRate\":\"", new BigDecimal("0.02"));
                BigDecimal buybackRate = parseDecimal(rules, "\"buybackRate\":\"", new BigDecimal("0.02"));
                BigDecimal platformRate = parseNestedDecimal(rules, "\"profitShare\":", "\"platform\":\"", new BigDecimal("0.021"));

                // 中奖：倍数 * 金额，扣费率
                BigDecimal gross = order.getAmount().multiply(multiplier);
                BigDecimal fee = gross.multiply(winFeeRate);
                BigDecimal net = gross.subtract(fee).setScale(18, RoundingMode.DOWN);

                // allocations：用户派奖、回购池、平台分润（其余分润对象由 n4 补齐）
                saveAllocation(order, issue, AllocationType.USER_PAYOUT, order.getUserId(), order.getAssetCode(), net,
                        "用户派奖(净额)");
                saveAllocation(order, issue, AllocationType.BUYBACK_POOL, 0L, order.getAssetCode(),
                        gross.multiply(buybackRate).setScale(18, RoundingMode.DOWN), "回购池");
                saveAllocation(order, issue, AllocationType.PROFIT_SHARE_PLATFORM, 0L, order.getAssetCode(),
                        gross.multiply(platformRate).setScale(18, RoundingMode.DOWN), "平台分润");
                allocateWinProfitShare(order, issue, gross, rules);

                walletLedgerService.credit(order.getUserId(), order.getAssetCode(), net,
                        WalletBizType.WIN_PAYOUT, "PAYOUT-" + order.getOrderNo(),
                        "中奖派奖(47x)-扣2%-" + order.getOrderNo());
            } else {
                BigDecimal fundRate = parseDecimal(rules, "\"fundRate\":\"", new BigDecimal("0.05"));

                // 不中奖：基金入账到待释放账户 FUND_SIX（占位：后续接 SIX 兑换与释放）
                BigDecimal fund = order.getAmount().multiply(fundRate)
                        .setScale(18, RoundingMode.DOWN);
                if (fund.compareTo(BigDecimal.ZERO) > 0) {
                    saveAllocation(order, issue, AllocationType.FUND_IN, order.getUserId(), AssetCode.FUND_SIX, fund,
                            "基金入账(待释放)");
                    walletLedgerService.credit(order.getUserId(), AssetCode.FUND_SIX, fund,
                            WalletBizType.LOST_FUND, "FUND-" + order.getOrderNo(),
                            "不中奖基金入账(待释放)-" + order.getOrderNo());
                    fundReleaseService.createPlan(order.getUserId(), order.getOrderNo(), fund);
                }

                allocateLoseRebates(order, issue, order.getAmount(), rules);
            }

            // 标记订单已结算
            baseMapper.update(null, Wrappers.<ActivityOrder>lambdaUpdate()
                    .set(ActivityOrder::getStatus, ActivityOrderStatus.SETTLED)
                    .set(ActivityOrder::getSettledAt, LocalDateTime.now())
                    .eq(ActivityOrder::getId, order.getId()));

            log.info("订单结算完成: orderNo={}", order.getOrderNo());
        }
    }

    private boolean isWinBySeed(String orderNo, long seed, int winRateBp) {
        int h = Math.abs((orderNo + ":" + seed).hashCode());
        int r = h % 10000;
        return r < Math.max(0, Math.min(10000, winRateBp));
    }

    private long parseLong(String payload, String keyPrefix, long defaultVal) {
        try {
            int idx = payload.indexOf(keyPrefix);
            if (idx < 0) return defaultVal;
            int start = idx + keyPrefix.length();
            int end = start;
            while (end < payload.length()) {
                char c = payload.charAt(end);
                if ((c >= '0' && c <= '9') || c == '-') {
                    end++;
                } else {
                    break;
                }
            }
            if (end <= start) return defaultVal;
            return Long.parseLong(payload.substring(start, end));
        } catch (Exception ignore) {
            return defaultVal;
        }
    }

    private void saveAllocation(ActivityOrder order, ActivityIssue issue, AllocationType type,
                                Long beneficiaryUserId, AssetCode assetCode, BigDecimal amount, String remark) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        RewardAllocation a = new RewardAllocation();
        a.setSourceBizNo(order.getOrderNo());
        a.setIssueId(issue.getId());
        a.setOrderId(order.getId());
        a.setType(type);
        a.setBeneficiaryUserId(beneficiaryUserId);
        a.setAssetCode(assetCode);
        a.setAmount(amount);
        a.setRemark(remark);
        rewardAllocationService.saveIdempotent(a);
    }

    private void allocateWinProfitShare(ActivityOrder order, ActivityIssue issue, BigDecimal gross, String rules) {
        BigDecimal agentRate = parseNestedDecimal(rules, "\"profitShare\":", "\"agent\":\"", new BigDecimal("0.01"));
        BigDecimal unionRate = parseNestedDecimal(rules, "\"profitShare\":", "\"union\":\"", new BigDecimal("0.005"));
        BigDecimal directorRate = parseNestedDecimal(rules, "\"profitShare\":", "\"director\":\"", new BigDecimal("0.003"));

        BigDecimal agentAmt = gross.multiply(agentRate).setScale(18, RoundingMode.DOWN);
        BigDecimal unionAmt = gross.multiply(unionRate).setScale(18, RoundingMode.DOWN);
        BigDecimal directorAmt = gross.multiply(directorRate).setScale(18, RoundingMode.DOWN);

        User agent = findFirstUplineByRole(order.getUserId(), com.debox.reward.modules.user.enums.UserRole.AGENT);
        if (agent != null) {
            saveAllocation(order, issue, AllocationType.PROFIT_SHARE_AGENT, agent.getId(), order.getAssetCode(),
                    agentAmt, "代理分润(1%)");
            walletLedgerService.credit(agent.getId(), order.getAssetCode(), agentAmt,
                    WalletBizType.ACTIVITY_REWARD, "PSA-" + order.getOrderNo(), "代理分润-中奖-" + order.getOrderNo());
        }

        User union = findFirstUplineByRole(order.getUserId(), com.debox.reward.modules.user.enums.UserRole.UNION);
        if (union != null) {
            saveAllocation(order, issue, AllocationType.PROFIT_SHARE_UNION, union.getId(), order.getAssetCode(),
                    unionAmt, "工会分润(0.5%)");
            walletLedgerService.credit(union.getId(), order.getAssetCode(), unionAmt,
                    WalletBizType.ACTIVITY_REWARD, "PSU-" + order.getOrderNo(), "工会分润-中奖-" + order.getOrderNo());
        }

        User director = findFirstUplineByRole(order.getUserId(), com.debox.reward.modules.user.enums.UserRole.DIRECTOR);
        if (director != null) {
            saveAllocation(order, issue, AllocationType.PROFIT_SHARE_DIRECTOR, director.getId(), order.getAssetCode(),
                    directorAmt, "董事分润(0.3%)");
            walletLedgerService.credit(director.getId(), order.getAssetCode(), directorAmt,
                    WalletBizType.ACTIVITY_REWARD, "PSD-" + order.getOrderNo(), "董事分润-中奖-" + order.getOrderNo());
        }
    }

    private void allocateLoseRebates(ActivityOrder order, ActivityIssue issue, BigDecimal base, String rules) {
        // 会员返水固定（不参与级差）
        BigDecimal memberRate = parseNestedDecimal(rules, "\"rebate\":", "\"member\":\"", new BigDecimal("0.02"));
        BigDecimal memberAmt = base.multiply(memberRate).setScale(18, RoundingMode.DOWN);
        saveAllocation(order, issue, AllocationType.REBATE_MEMBER, order.getUserId(), order.getAssetCode(), memberAmt, "会员返水");
        walletLedgerService.credit(order.getUserId(), order.getAssetCode(), memberAmt,
                WalletBizType.ACTIVITY_REWARD, "RBM-" + order.getOrderNo(), "会员返水-不中奖-" + order.getOrderNo());

        // 级差：从近到远遍历上级，按角色目标比例发放差额（SHOP 3% < AGENT 4%）
        BigDecimal shopRate = parseNestedDecimal(rules, "\"rebate\":", "\"shop\":\"", new BigDecimal("0.03"));
        BigDecimal agentRate = parseNestedDecimal(rules, "\"rebate\":", "\"agent\":\"", new BigDecimal("0.04"));
        BigDecimal allocatedRate = BigDecimal.ZERO;

        User firstShop = null;
        User firstAgent = null;
        User secondShop = null;
        User secondAgent = null;

        Long cur = order.getUserId();
        for (int i = 0; i < 20; i++) {
            User u = userMapper.selectById(cur);
            if (u == null || u.getParentId() == null) break;
            User p = userMapper.selectById(u.getParentId());
            if (p == null) break;

            BigDecimal target = null;
            AllocationType type = null;
            if (p.getRole() == com.debox.reward.modules.user.enums.UserRole.SHOP) {
                target = shopRate;
                type = AllocationType.REBATE_SHOP;
                if (firstShop == null) firstShop = p; else if (secondShop == null) secondShop = p;
            } else if (p.getRole() == com.debox.reward.modules.user.enums.UserRole.AGENT) {
                target = agentRate;
                type = AllocationType.REBATE_AGENT;
                if (firstAgent == null) firstAgent = p; else if (secondAgent == null) secondAgent = p;
            }

            if (target != null && target.compareTo(allocatedRate) > 0) {
                BigDecimal diff = target.subtract(allocatedRate);
                BigDecimal amt = base.multiply(diff).setScale(18, RoundingMode.DOWN);
                saveAllocation(order, issue, type, p.getId(), order.getAssetCode(), amt, "级差返水");
                walletLedgerService.credit(p.getId(), order.getAssetCode(), amt,
                        WalletBizType.ACTIVITY_REWARD, "RBD-" + order.getOrderNo() + "-" + p.getId(),
                        "级差返水-不中奖-" + order.getOrderNo());
                allocatedRate = target;
            }

            cur = p.getId();
        }

        // 平级奖（占位实现）：同角色第二个节点拿平级比例
        BigDecimal peerShop = parseNestedDecimal(rules, "\"peerBonus\":", "\"shop\":\"", new BigDecimal("0.003"));
        BigDecimal peerAgent = parseNestedDecimal(rules, "\"peerBonus\":", "\"agent\":\"", new BigDecimal("0.004"));
        if (secondShop != null && peerShop.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal amt = base.multiply(peerShop).setScale(18, RoundingMode.DOWN);
            saveAllocation(order, issue, AllocationType.PEER_BONUS, secondShop.getId(), order.getAssetCode(), amt, "平级奖-小庄");
            walletLedgerService.credit(secondShop.getId(), order.getAssetCode(), amt,
                    WalletBizType.ACTIVITY_REWARD, "PBS-" + order.getOrderNo() + "-" + secondShop.getId(),
                    "平级奖-小庄-" + order.getOrderNo());
        }
        if (secondAgent != null && peerAgent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal amt = base.multiply(peerAgent).setScale(18, RoundingMode.DOWN);
            saveAllocation(order, issue, AllocationType.PEER_BONUS, secondAgent.getId(), order.getAssetCode(), amt, "平级奖-代理");
            walletLedgerService.credit(secondAgent.getId(), order.getAssetCode(), amt,
                    WalletBizType.ACTIVITY_REWARD, "PBA-" + order.getOrderNo() + "-" + secondAgent.getId(),
                    "平级奖-代理-" + order.getOrderNo());
        }
    }

    private User findFirstUplineByRole(Long userId, com.debox.reward.modules.user.enums.UserRole role) {
        Long cur = userId;
        for (int i = 0; i < 20; i++) {
            User u = userMapper.selectById(cur);
            if (u == null) return null;
            Long p = u.getParentId();
            if (p == null) return null;
            User parent = userMapper.selectById(p);
            if (parent == null) return null;
            if (parent.getRole() == role) {
                return parent;
            }
            cur = parent.getId();
        }
        return null;
    }

    private Long parseLongObj(String payload, String keyPrefix) {
        long v = parseLong(payload, keyPrefix, Long.MIN_VALUE);
        return v == Long.MIN_VALUE ? null : v;
    }

    private BigDecimal parseDecimal(String rules, String keyPrefix, BigDecimal defaultVal) {
        if (rules == null) return defaultVal;
        try {
            int idx = rules.indexOf(keyPrefix);
            if (idx < 0) return defaultVal;
            int start = idx + keyPrefix.length();
            int end = rules.indexOf("\"", start);
            if (end < 0) return defaultVal;
            return new BigDecimal(rules.substring(start, end));
        } catch (Exception ignore) {
            return defaultVal;
        }
    }

    private BigDecimal parseNestedDecimal(String rules, String objectKey, String keyPrefix, BigDecimal defaultVal) {
        if (rules == null) return defaultVal;
        int objIdx = rules.indexOf(objectKey);
        if (objIdx < 0) return defaultVal;
        int brace = rules.indexOf("{", objIdx);
        if (brace < 0) return defaultVal;
        int endObj = rules.indexOf("}", brace);
        if (endObj < 0) return defaultVal;
        String sub = rules.substring(brace, endObj + 1);
        return parseDecimal(sub, keyPrefix, defaultVal);
    }
}
