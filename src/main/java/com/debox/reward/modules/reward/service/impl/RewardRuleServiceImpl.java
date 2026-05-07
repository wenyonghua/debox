package com.debox.reward.modules.reward.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.debox.reward.modules.reward.entity.RewardRule;
import com.debox.reward.modules.reward.mapper.RewardRuleMapper;
import com.debox.reward.modules.reward.service.RewardRuleService;
import org.springframework.stereotype.Service;

@Service
public class RewardRuleServiceImpl extends ServiceImpl<RewardRuleMapper, RewardRule> implements RewardRuleService {
}
