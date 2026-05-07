package com.debox.reward.modules.activity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.debox.reward.modules.activity.entity.ActivityIssue;
import com.debox.reward.modules.activity.enums.ActivityIssueStatus;
import com.debox.reward.modules.activity.mapper.ActivityIssueMapper;
import com.debox.reward.modules.activity.service.ActivityIssueService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityIssueServiceImpl extends ServiceImpl<ActivityIssueMapper, ActivityIssue>
        implements ActivityIssueService {

    @Override
    public ActivityIssue getOpenIssue() {
        return getOne(Wrappers.<ActivityIssue>lambdaQuery()
                .eq(ActivityIssue::getStatus, ActivityIssueStatus.OPEN)
                .orderByAsc(ActivityIssue::getEndTime)
                .last("limit 1"));
    }

    @Override
    public List<ActivityIssue> listPendingClose() {
        return list(Wrappers.<ActivityIssue>lambdaQuery()
                .eq(ActivityIssue::getStatus, ActivityIssueStatus.OPEN)
                .lt(ActivityIssue::getEndTime, LocalDateTime.now()));
    }

    @Override
    public List<ActivityIssue> listPendingDraw() {
        return list(Wrappers.<ActivityIssue>lambdaQuery()
                .eq(ActivityIssue::getStatus, ActivityIssueStatus.OPEN)
                .isNull(ActivityIssue::getDrawnAt)
                .lt(ActivityIssue::getEndTime, LocalDateTime.now()));
    }

    @Override
    public boolean markDrawn(Long issueId, String resultPayload) {
        int rows = baseMapper.update(null,
                Wrappers.<ActivityIssue>lambdaUpdate()
                        .set(ActivityIssue::getResultPayload, resultPayload)
                        .set(ActivityIssue::getDrawnAt, LocalDateTime.now())
                        .set(ActivityIssue::getUpdatedAt, LocalDateTime.now())
                        .eq(ActivityIssue::getId, issueId)
                        .isNull(ActivityIssue::getDrawnAt)
                        .eq(ActivityIssue::getStatus, ActivityIssueStatus.OPEN));
        return rows > 0;
    }

    @Override
    public boolean markSettling(Long issueId) {
        int rows = baseMapper.update(null,
                Wrappers.<ActivityIssue>lambdaUpdate()
                        .set(ActivityIssue::getStatus, ActivityIssueStatus.SETTLING)
                        .set(ActivityIssue::getUpdatedAt, LocalDateTime.now())
                        .eq(ActivityIssue::getId, issueId)
                        .eq(ActivityIssue::getStatus, ActivityIssueStatus.OPEN));
        return rows > 0;
    }

    @Override
    public void markSettled(Long issueId) {
        baseMapper.update(null,
                Wrappers.<ActivityIssue>lambdaUpdate()
                        .set(ActivityIssue::getStatus, ActivityIssueStatus.SETTLED)
                        .set(ActivityIssue::getSettleTime, LocalDateTime.now())
                        .set(ActivityIssue::getUpdatedAt, LocalDateTime.now())
                        .eq(ActivityIssue::getId, issueId));
    }

    @Override
    public List<ActivityIssue> listIncompleteSettling() {
        return list(Wrappers.<ActivityIssue>lambdaQuery()
                .eq(ActivityIssue::getStatus, ActivityIssueStatus.SETTLING)
                .orderByAsc(ActivityIssue::getEndTime));
    }
}
