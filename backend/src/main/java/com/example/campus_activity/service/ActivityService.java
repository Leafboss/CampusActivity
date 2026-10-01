package com.example.campus_activity.service;

import com.example.campus_activity.pojo.Activity;

import java.util.List;

/**
 * 活动业务接口：controller 只依赖这个接口，不关心实现细节
 */
public interface ActivityService {

    /** 查询全部活动 */
    List<Activity> list();

    /** 按 id 查详情 */
    Activity getById(Long id);

    /** 新增活动 */
    void add(Activity activity);

    /** 更新活动 */
    void update(Activity activity);

    /** 删除活动 */
    void delete(Long id);
}
