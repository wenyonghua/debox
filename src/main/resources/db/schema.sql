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
