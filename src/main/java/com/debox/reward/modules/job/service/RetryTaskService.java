package com.debox.reward.modules.job.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.debox.reward.modules.job.entity.RetryTask;

public interface RetryTaskService extends IService<RetryTask> {

    /** 单笔订单结算失败后入队；bizKey 幂等 */
    void enqueueOrderSettlement(Long orderId, Long issueId, String errorSummary);

    /** 调度消费：到期且 PENDING 的任务 */
    void processDueTasks(int batchSize);
}
