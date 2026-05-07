package com.debox.reward.modules.reward.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.debox.reward.modules.reward.entity.RewardAllocation;
import com.debox.reward.modules.reward.mapper.RewardAllocationMapper;
import com.debox.reward.modules.reward.service.RewardAllocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class RewardAllocationServiceImpl extends ServiceImpl<RewardAllocationMapper, RewardAllocation>
        implements RewardAllocationService {

    @Override
    public void saveIdempotent(RewardAllocation allocation) {
        Long cnt = baseMapper.selectCount(Wrappers.<RewardAllocation>lambdaQuery()
                .eq(RewardAllocation::getSourceBizNo, allocation.getSourceBizNo())
                .eq(RewardAllocation::getType, allocation.getType())
                .eq(RewardAllocation::getBeneficiaryUserId, allocation.getBeneficiaryUserId())
                .eq(RewardAllocation::getAssetCode, allocation.getAssetCode()));
        if (cnt != null && cnt > 0) {
            log.warn("allocation 幂等重复，跳过: sourceBizNo={}, type={}, beneficiary={}",
                    allocation.getSourceBizNo(), allocation.getType(), allocation.getBeneficiaryUserId());
            return;
        }
        if (allocation.getCreatedAt() == null) {
            allocation.setCreatedAt(LocalDateTime.now());
        }
        if (allocation.getStatus() == null) {
            allocation.setStatus(0);
        }
        save(allocation);
    }
}

