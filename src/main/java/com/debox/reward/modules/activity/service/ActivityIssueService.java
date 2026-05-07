package com.debox.reward.modules.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.debox.reward.modules.activity.entity.ActivityIssue;

import java.util.List;

public interface ActivityIssueService extends IService<ActivityIssue> {

    ActivityIssue getOpenIssue();

    /**
     * 查询已结束但尚未结算的活动期号（end_time < now, status=OPEN）
     */
    List<ActivityIssue> listPendingClose();

    /**
     * 查询已到开奖时间但尚未开奖的期号（end_time < now, status=OPEN, drawn_at is null）
     */
    List<ActivityIssue> listPendingDraw();

    /**
     * 写入开奖结果（仅当尚未开奖时写入）
     *
     * @return true 表示写入成功
     */
    boolean markDrawn(Long issueId, String resultPayload);

    /**
     * 将期号标记为 SETTLING（开始结算），返回 false 表示状态不匹配
     */
    boolean markSettling(Long issueId);

    /**
     * 将期号标记为 SETTLED（结算完成）
     */
    void markSettled(Long issueId);
}
