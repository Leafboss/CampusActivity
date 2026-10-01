package com.example.campus_activity.exception;

import com.example.campus_activity.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：任何一层抛出的异常都会在这里被接住，
 * 统一返回友好提示，避免把堆栈信息直接暴露给前端。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHander {

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        // 堆栈打到后端日志里方便排查，返回给前端的只有友好提示
        log.error("服务异常：", e);
        return Result.error("出错了，请稍后再试~");
    }
}
