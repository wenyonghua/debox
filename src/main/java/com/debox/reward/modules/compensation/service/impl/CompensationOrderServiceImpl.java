package com.debox.reward.modules.compensation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.admin.service.AdminAuditLogService;
import com.debox.reward.modules.auth.security.SecurityUtils;
import com.debox.reward.modules.compensation.dto.CompensationCreateRequest;
import com.debox.reward.modules.compensation.entity.CompensationOrder;
import com.debox.reward.modules.compensation.enums.CompensationOrderStatus;
import com.debox.reward.modules.compensation.mapper.CompensationOrderMapper;
import com.debox.reward.modules.compensation.service.CompensationOrderService;
import com.debox.reward.modules.wallet.enums.WalletBizType;
import com.debox.reward.modules.wallet.service.WalletLedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompensationOrderServiceImpl extends ServiceImpl<CompensationOrderMapper, CompensationOrder>
        implements CompensationOrderService {

    private final WalletLedgerService walletLedgerService;
    private final AdminAuditLogService adminAuditLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompensationOrder createPending(CompensationCreateRequest req) {
        if (req.getAmount().signum() <= 0) {
            throw new BizException("amount 必须大于 0");
        }
        Long adminId = SecurityUtils.currentUserId();
        String bizNo = "CP" + System.currentTimeMillis();

        CompensationOrder co = new CompensationOrder();
        co.setBizNo(bizNo);
        co.setUserId(req.getUserId());
        co.setAssetCode(req.getAssetCode());
        co.setDirection(req.getDirection().toLowerCase());
        co.setAmount(req.getAmount());
        co.setRemark(req.getRemark());
        co.setStatus(CompensationOrderStatus.PENDING);
        co.setCreatedBy(adminId);
        co.setCreatedAt(LocalDateTime.now());
        co.setUpdatedAt(LocalDateTime.now());
        save(co);

        adminAuditLogService.log("COMP_CREATE", "compensation_order", String.valueOf(co.getId()),
                null, "{\"bizNo\":\"" + bizNo + "\"}");
        return co;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveAndExecute(Long id) {
        CompensationOrder co = getById(id);
        if (co == null || co.getStatus() != CompensationOrderStatus.PENDING) {
            throw new BizException("补偿单不存在或状态不可审批");
        }

        Long approver = SecurityUtils.currentUserId();
        co.setApprovedBy(approver);
        co.setStatus(CompensationOrderStatus.APPROVED);
        co.setUpdatedAt(LocalDateTime.now());
        updateById(co);

        String execBizNo = "EXEC-" + co.getBizNo();
        try {
            if ("credit".equals(co.getDirection())) {
                walletLedgerService.credit(co.getUserId(), co.getAssetCode(), co.getAmount(),
                        WalletBizType.ADJUST, execBizNo, "补偿入账-" + co.getBizNo());
            } else if ("debit".equals(co.getDirection())) {
                walletLedgerService.debit(co.getUserId(), co.getAssetCode(), co.getAmount(),
                        WalletBizType.ADJUST, execBizNo, "补偿扣账-" + co.getBizNo());
            } else {
                throw new BizException("direction 仅支持 credit/debit");
            }
            co.setExecutedBizNo(execBizNo);
            co.setStatus(CompensationOrderStatus.EXECUTED);
            co.setUpdatedAt(LocalDateTime.now());
            updateById(co);
            adminAuditLogService.log("COMP_APPROVE_EXECUTE", "compensation_order", String.valueOf(id),
                    null, "{\"execBizNo\":\"" + execBizNo + "\"}");
        } catch (Exception e) {
            co.setStatus(CompensationOrderStatus.EXEC_FAILED);
            co.setUpdatedAt(LocalDateTime.now());
            updateById(co);
            adminAuditLogService.log("COMP_EXEC_FAILED", "compensation_order", String.valueOf(id),
                    null, "\"" + escape(e.getMessage()) + "\"");
            throw new BizException("补偿执行失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String reason) {
        CompensationOrder co = getById(id);
        if (co == null || co.getStatus() != CompensationOrderStatus.PENDING) {
            throw new BizException("补偿单不存在或状态不可拒绝");
        }
        co.setStatus(CompensationOrderStatus.REJECTED);
        co.setRejectionReason(reason);
        co.setUpdatedAt(LocalDateTime.now());
        updateById(co);
        adminAuditLogService.log("COMP_REJECT", "compensation_order", String.valueOf(id), null,
                reason == null ? "{}" : "\"" + escape(reason) + "\"");
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "'");
    }
}
