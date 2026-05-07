package com.debox.reward.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.debox.reward.modules.user.dto.RegisterRequest;
import com.debox.reward.modules.user.entity.User;

public interface UserService extends IService<User> {

    User register(RegisterRequest request);
}
