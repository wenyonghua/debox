package com.debox.reward.modules.activity.dto;

import com.debox.reward.modules.activity.entity.ActivityOrder;
import com.debox.reward.modules.reward.entity.RewardAllocation;
import lombok.Data;

import java.util.List;

@Data
public class OrderDetailResponse {
    private ActivityOrder order;
    private List<RewardAllocation> allocations;
}

