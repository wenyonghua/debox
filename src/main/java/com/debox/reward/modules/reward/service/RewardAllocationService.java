package com.debox.reward.modules.reward.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.debox.reward.modules.reward.entity.RewardAllocation;

public interface RewardAllocationService extends IService<RewardAllocation> {

    /**
     * 幂等写入 allocation：同 sourceBizNo + type + beneficiary + asset 重复则跳过
     */
    void saveIdempotent(RewardAllocation allocation);
}

