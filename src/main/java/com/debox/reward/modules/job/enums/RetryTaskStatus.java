package com.debox.reward.modules.job.enums;

public enum RetryTaskStatus {
    PENDING,
    DONE,
    /** 已达最大次数 */
    EXHAUSTED,
    FAILED
}
