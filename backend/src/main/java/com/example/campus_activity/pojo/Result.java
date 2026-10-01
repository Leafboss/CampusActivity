package com.example.campus_activity.pojo;

import lombok.Data;

/**
 * 统一响应体：所有接口都返回 {code, msg, data} 三段结构
 * code = 1 成功，0 失败；前端按 code 判断后取 data
 */
@Data
public class Result {

    private Integer code;
    private String msg;
    private Object data;

    public static Result success() {
        return success(null);
    }

    public static Result success(Object data) {
        Result r = new Result();
        r.code = 1;
        r.msg = "success";
        r.data = data;
        return r;
    }

    public static Result error(String msg) {
        Result r = new Result();
        r.code = 0;
        r.msg = msg;
        r.data = null;
        return r;
    }
}
