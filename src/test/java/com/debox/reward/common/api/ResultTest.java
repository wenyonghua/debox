package com.debox.reward.common.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultTest {

    @Test
    void okShouldReturnSuccessCode() {
        Result<String> result = Result.ok("pong");
        assertEquals(0, result.getCode());
        assertEquals("pong", result.getData());
    }
}