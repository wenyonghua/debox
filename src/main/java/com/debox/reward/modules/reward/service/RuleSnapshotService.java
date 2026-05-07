package com.debox.reward.modules.reward.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.debox.reward.modules.reward.entity.RuleSnapshot;

public interface RuleSnapshotService extends IService<RuleSnapshot> {

    RuleSnapshot createSnapshot(String version, String payloadJson);
}

