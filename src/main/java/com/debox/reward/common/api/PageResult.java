package com.debox.reward.common.api;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private long page;
    private long size;
    private long total;
    private List<T> records;
}

