package com.debox.reward.common.exception;

import com.debox.reward.common.api.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return Result.fail(ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public Result<Void> handleValidationException(Exception ex) {
        log.warn("参数校验失败: {}", ex.getMessage());
        return Result.fail("参数校验失败");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("系统异常", ex);   // ← 关键：打印完整堆栈，之前这里什么都没有
        return Result.fail("系统繁忙，请稍后再试");
    }
}
