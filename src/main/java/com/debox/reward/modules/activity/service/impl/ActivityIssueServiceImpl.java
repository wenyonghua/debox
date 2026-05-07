package com.debox.reward.modules.activity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.debox.reward.modules.activity.entity.ActivityIssue;
import com.debox.reward.modules.activity.enums.ActivityIssueStatus;
import com.debox.reward.modules.activity.mapper.ActivityIssueMapper;
import com.debox.reward.modules.activity.service.ActivityIssueService;
import org.springframework.stereotype.Service;

@Service
public class ActivityIssueServiceImpl extends ServiceImpl<ActivityIssueMapper, ActivityIssue> implements ActivityIssueService {

    @Override
    public ActivityIssue getOpenIssue() {
        return getOne(Wrappers.<ActivityIssue>lambdaQuery()
                .eq(ActivityIssue::getStatus, ActivityIssueStatus.OPEN)
                .orderByAsc(ActivityIssue::getEndTime)
                .last("limit 1"));
    }
}
