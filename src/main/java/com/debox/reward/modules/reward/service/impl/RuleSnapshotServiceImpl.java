package com.debox.reward.modules.reward.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.debox.reward.modules.reward.entity.RuleSnapshot;
import com.debox.reward.modules.reward.mapper.RuleSnapshotMapper;
import com.debox.reward.modules.reward.service.RuleSnapshotService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RuleSnapshotServiceImpl extends ServiceImpl<RuleSnapshotMapper, RuleSnapshot>
        implements RuleSnapshotService {

    @Override
    public RuleSnapshot createSnapshot(String version, String payloadJson) {
        RuleSnapshot s = new RuleSnapshot();
        s.setVersion(version);
        s.setPayloadJson(payloadJson);
        s.setCreatedAt(LocalDateTime.now());
        save(s);
        return s;
    }
}

