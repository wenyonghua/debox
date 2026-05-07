package com.debox.reward.modules.reward.controller;

import com.debox.reward.common.api.Result;
import com.debox.reward.modules.reward.entity.RewardRule;
import com.debox.reward.modules.reward.service.RewardRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reward/rules")
@RequiredArgsConstructor
public class RewardRuleController {

    private final RewardRuleService rewardRuleService;

    @GetMapping
    public Result<List<RewardRule>> list() {
        return Result.ok(rewardRuleService.list());
    }
}
