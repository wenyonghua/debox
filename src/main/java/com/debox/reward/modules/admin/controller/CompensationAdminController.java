package com.debox.reward.modules.admin.controller;

import com.debox.reward.common.api.Result;
import com.debox.reward.modules.compensation.dto.CompensationCreateRequest;
import com.debox.reward.modules.compensation.dto.CompensationRejectRequest;
import com.debox.reward.modules.compensation.entity.CompensationOrder;
import com.debox.reward.modules.compensation.service.CompensationOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/compensations")
@RequiredArgsConstructor
public class CompensationAdminController {

    private final CompensationOrderService compensationOrderService;

    @PostMapping
    public Result<CompensationOrder> create(@Valid @RequestBody CompensationCreateRequest request) {
        return Result.ok(compensationOrderService.createPending(request));
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        compensationOrderService.approveAndExecute(id);
        return Result.ok(null);
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody(required = false) CompensationRejectRequest req) {
        compensationOrderService.reject(id, req == null ? null : req.getReason());
        return Result.ok(null);
    }
}
