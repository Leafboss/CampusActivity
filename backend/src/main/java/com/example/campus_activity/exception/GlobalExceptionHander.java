package com.example.campus_activity.exception;

import com.example.campus_activity.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：任何一层抛出的异常都会在这里被接住，
 * 统一返回友好提示，避免把堆栈信息直接暴露给前端。
 *
 * 处理顺序：Spring 会挑「最匹配」的那个 @ExceptionHandler，所以下面三个分支里，
 * 越具体的异常越先命中，最后才轮到兜底的 Exception。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHander {

    /**
     * 参数校验不通过（Controller 上的 @Valid 触发）。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidation(MethodArgumentNotValidException e) {
        // 一次只提示第一条字段错误：前端是逐条弹提示，多字段同时错时没必要全吐出去
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = (fieldError == null) ? "请求参数不合法" : fieldError.getDefaultMessage();
        log.warn("参数校验不通过：{}", msg);
        return Result.error(msg);
    }

    /**
     * 请求体读不出来：JSON 语法错误、字段类型对不上
     * （比如把 time 传成 "明天下午"，或者把 status 传成 "abc"）。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败：{}", e.getMessage());
        return Result.error("请求参数格式不正确，请检查时间格式或status是否为0或1（时间格式示例：2026-10-15 19:00）");
    }

    /**
     * 查询参数类型对不上：例如 GET /api/activities?status=abc（status 要的是数字）。
     * 不加这一条就会落到下面的兜底分支，用户只能看到「出错了，请稍后再试~」。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型不正确：{} = {}", e.getName(), e.getValue());
        return Result.error("参数 " + e.getName() + " 的格式不正确");
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        // 堆栈打到后端日志里方便排查，返回给前端的只有友好提示
        log.error("服务异常：", e);
        return Result.error("出错了，请稍后再试~");
    }
}