package com.debox.reward.modules.activity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.debox.reward.modules.activity.entity.ActivityIssue;

public interface ActivityIssueService extends IService<ActivityIssue> {

    ActivityIssue getOpenIssue();
}
