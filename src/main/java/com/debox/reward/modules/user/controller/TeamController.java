package com.debox.reward.modules.user.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.debox.reward.common.api.PageResult;
import com.debox.reward.common.api.Result;
import com.debox.reward.common.exception.BizException;
import com.debox.reward.modules.auth.security.SecurityUtils;
import com.debox.reward.modules.user.dto.TeamSummaryResponse;
import com.debox.reward.modules.user.entity.User;
import com.debox.reward.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/team")
@RequiredArgsConstructor
public class TeamController {

    private final UserService userService;

    @GetMapping("/summary")
    public Result<TeamSummaryResponse> summary() {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) {
            throw new BizException("未登录");
        }
        long count = userService.count(Wrappers.<User>lambdaQuery().eq(User::getParentId, uid));
        TeamSummaryResponse r = new TeamSummaryResponse();
        r.setDirectCount(count);
        return Result.ok(r);
    }

    @GetMapping("/members")
    public Result<PageResult<User>> members(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "20") long size) {
        Long uid = SecurityUtils.currentUserId();
        if (uid == null) {
            throw new BizException("未登录");
        }
        long p = Math.max(1, page);
        long s = Math.max(1, Math.min(200, size));
        Page<User> pg = userService.page(new Page<>(p, s),
                Wrappers.<User>lambdaQuery().eq(User::getParentId, uid).orderByDesc(User::getId));
        PageResult<User> pr = new PageResult<>();
        pr.setPage(p);
        pr.setSize(s);
        pr.setTotal(pg.getTotal());
        pr.setRecords(pg.getRecords());
        return Result.ok(pr);
    }
}
