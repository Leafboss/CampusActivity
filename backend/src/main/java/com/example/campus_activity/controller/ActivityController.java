package com.example.campus_activity.controller;

import com.example.campus_activity.pojo.Activity;
import com.example.campus_activity.pojo.Result;
import com.example.campus_activity.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动接口：只做「收参数 -> 调 service -> 包装 Result」，不写业务逻辑。
 * 全部接口以 /api 开头，与前端页面路径区分开。
 */
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    /** 活动列表：GET /api/activities */
    @GetMapping
    public Result list() {
        List<Activity> list = activityService.list();
        return Result.success(list);
    }

    /** 活动详情：GET /api/activities/{id} */
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        Activity activity = activityService.getById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        return Result.success(activity);
    }

    /** 新增活动：POST /api/activities，请求体为活动 JSON */
    @PostMapping
    public Result add(@RequestBody Activity activity) {
        activityService.add(activity);
        return Result.success();
    }

    /** 修改活动：PUT /api/activities/{id}，请求体为活动 JSON */
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Activity activity) {
        // id 以路径为准，防止请求体里带错
        activity.setId(id);
        activityService.update(activity);
        return Result.success();
    }

    /** 删除活动：DELETE /api/activities/{id} */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        activityService.delete(id);
        return Result.success();
    }
}
