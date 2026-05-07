package com.debox.reward.modules.buyback.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.buyback.entity.BuybackExecution;
import com.debox.reward.modules.buyback.entity.BuybackPool;
import com.debox.reward.modules.buyback.mapper.BuybackExecutionMapper;
import com.debox.reward.modules.buyback.mapper.BuybackPoolMapper;
import com.debox.reward.modules.buyback.service.BuybackExecutionAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BuybackExecutionAdminServiceImpl implements BuybackExecutionAdminService {

    private static final int POOL_ID = 1;

    private final BuybackPoolMapper buybackPoolMapper;
    private final BuybackExecutionMapper buybackExecutionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BuybackExecution executeStub(BigDecimal amountUsdt, String remark) {
        if (amountUsdt == null || amountUsdt.signum() <= 0) {
            throw new BizException("amount 必须大于0");
        }
        BuybackPool pool = buybackPoolMapper.selectById(POOL_ID);
        if (pool == null) {
            throw new BizException("回购池未初始化");
        }
        if (pool.getBalance().compareTo(amountUsdt) < 0) {
            throw new BizException("回购池余额不足");
        }
        BigDecimal newBal = pool.getBalance().subtract(amountUsdt);
        int rows = buybackPoolMapper.update(null, Wrappers.<BuybackPool>lambdaUpdate()
                .set(BuybackPool::getBalance, newBal)
                .set(BuybackPool::getUpdatedAt, LocalDateTime.now())
                .eq(BuybackPool::getId, POOL_ID)
                .eq(BuybackPool::getBalance, pool.getBalance()));
        if (rows != 1) {
            throw new BizException("回购池更新失败（并发或余额变化），请重试");
        }

        BuybackExecution ex = new BuybackExecution();
        ex.setBizNo("BBK-" + UUID.randomUUID().toString().replace("-", ""));
        ex.setAmountUsdt(amountUsdt);
        ex.setStatus("STUB_DONE");
        ex.setSwapTxHash(null);
        ex.setBurnTxHash(null);
        ex.setRemark(remark);
        ex.setCreatedAt(LocalDateTime.now());
        buybackExecutionMapper.insert(ex);
        return ex;
    }
}
