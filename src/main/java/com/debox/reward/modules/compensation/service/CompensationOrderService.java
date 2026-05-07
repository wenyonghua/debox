package com.debox.reward.modules.compensation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.debox.reward.modules.compensation.dto.CompensationCreateRequest;
import com.debox.reward.modules.compensation.entity.CompensationOrder;

public interface CompensationOrderService extends IService<CompensationOrder> {

    CompensationOrder createPending(CompensationCreateRequest req);

    void approveAndExecute(Long id);

    void reject(Long id, String reason);
}
