CREATE DATABASE IF NOT EXISTS debox_reward DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE debox_reward;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_no VARCHAR(32) NOT NULL UNIQUE,
    username VARCHAR(64) NOT NULL UNIQUE,
    mobile VARCHAR(32) NULL,
    password_hash VARCHAR(255) NULL,
    invite_code VARCHAR(32) NOT NULL UNIQUE,
    parent_id BIGINT NULL,
    role VARCHAR(32) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_parent_id (parent_id),
    INDEX idx_invite_code (invite_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS user_invite_relation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    parent_id BIGINT NULL,
    depth INT NOT NULL DEFAULT 0,
    path VARCHAR(512) NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请关系快照';

CREATE TABLE IF NOT EXISTS wallet_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    asset_code VARCHAR(32) NOT NULL,
    available_balance DECIMAL(36,18) NOT NULL DEFAULT 0,
    frozen_balance DECIMAL(36,18) NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_user_asset (user_id, asset_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包账户';

CREATE TABLE IF NOT EXISTS wallet_ledger (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    asset_code VARCHAR(32) NOT NULL,
    change_amount DECIMAL(36,18) NOT NULL,
    balance_after DECIMAL(36,18) NOT NULL,
    biz_type VARCHAR(32) NOT NULL,
    biz_no VARCHAR(64) NOT NULL,
    remark VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_biz_no (biz_no),
    INDEX idx_user_asset (user_id, asset_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包流水';

CREATE TABLE IF NOT EXISTS activity_issue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    issue_no VARCHAR(64) NOT NULL UNIQUE,
    title VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    result_payload JSON NULL,
    drawn_at DATETIME NULL,
    rule_snapshot_id BIGINT NULL,
    settle_time DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_status_end_time (status, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动期号';

CREATE TABLE IF NOT EXISTS activity_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    issue_id BIGINT NOT NULL,
    asset_code VARCHAR(32) NOT NULL,
    amount DECIMAL(36,18) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at DATETIME NOT NULL,
    settled_at DATETIME NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_issue_status (issue_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动订单';

CREATE TABLE IF NOT EXISTS reward_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(64) NOT NULL,
    user_role VARCHAR(32) NOT NULL,
    reward_asset_code VARCHAR(32) NOT NULL,
    reward_rate DECIMAL(18,8) NOT NULL DEFAULT 0,
    max_reward_amount DECIMAL(36,18) NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    effective_at DATETIME NULL,
    expired_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_event_role (event_type, user_role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖励规则';

CREATE TABLE IF NOT EXISTS reward_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reward_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    source_biz_no VARCHAR(64) NOT NULL,
    asset_code VARCHAR(32) NOT NULL,
    amount DECIMAL(36,18) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    granted_at DATETIME NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_source_biz_no (source_biz_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='奖励记录';

CREATE TABLE IF NOT EXISTS reward_allocation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_biz_no VARCHAR(64) NOT NULL,
    issue_id BIGINT NULL,
    order_id BIGINT NULL,
    type VARCHAR(64) NOT NULL,
    beneficiary_user_id BIGINT NULL,
    asset_code VARCHAR(32) NOT NULL,
    amount DECIMAL(36,18) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    remark VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    posted_at DATETIME NULL,
    INDEX idx_source_biz (source_biz_no),
    INDEX idx_issue_order (issue_id, order_id),
    INDEX idx_beneficiary (beneficiary_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分配明细（allocations）';

CREATE TABLE IF NOT EXISTS rule_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version VARCHAR(64) NOT NULL,
    payload_json JSON NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则快照';

CREATE TABLE IF NOT EXISTS fund_release_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    source_biz_no VARCHAR(64) NOT NULL,
    total_amount DECIMAL(36,18) NOT NULL,
    remaining_amount DECIMAL(36,18) NOT NULL,
    daily_rate DECIMAL(18,8) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_user_status (user_id, status),
    UNIQUE KEY uk_source_biz (source_biz_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基金释放计划';

CREATE TABLE IF NOT EXISTS fund_release_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    release_date DATE NOT NULL,
    amount DECIMAL(36,18) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    biz_no VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL,
    posted_at DATETIME NULL,
    UNIQUE KEY uk_plan_date (plan_id, release_date),
    UNIQUE KEY uk_biz_no (biz_no),
    INDEX idx_user_date (user_id, release_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='基金释放事件';

CREATE TABLE IF NOT EXISTS retry_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    biz_type VARCHAR(32) NOT NULL,
    biz_key VARCHAR(128) NOT NULL,
    order_id BIGINT NULL,
    issue_id BIGINT NULL,
    last_error VARCHAR(2048) NULL,
    status VARCHAR(32) NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 144,
    next_retry_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_biz_key (biz_key),
    INDEX idx_status_next (status, next_retry_at),
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='失败重试任务';

CREATE TABLE IF NOT EXISTS compensation_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    biz_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    asset_code VARCHAR(32) NOT NULL,
    direction VARCHAR(16) NOT NULL,
    amount DECIMAL(36,18) NOT NULL,
    remark VARCHAR(512) NULL,
    status VARCHAR(32) NOT NULL,
    created_by BIGINT NULL,
    approved_by BIGINT NULL,
    executed_biz_no VARCHAR(128) NULL,
    rejection_reason VARCHAR(512) NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补偿单审批';

CREATE TABLE IF NOT EXISTS admin_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    admin_id BIGINT NULL,
    action VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NULL,
    target_id VARCHAR(64) NULL,
    before_json JSON NULL,
    after_json JSON NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_admin (admin_id),
    INDEX idx_action (action),
    INDEX idx_target (target_type, target_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作审计日志';
