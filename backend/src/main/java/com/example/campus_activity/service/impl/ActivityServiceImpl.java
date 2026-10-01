package com.example.campus_activity.service.impl;

import com.example.campus_activity.mapper.ActivityMapper;
import com.example.campus_activity.pojo.Activity;
import com.example.campus_activity.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 活动业务实现：本项目业务很轻，目前只是对 mapper 的一层封装。
 * 保留 service 层是为了分层规范——以后加规则（如时间校验、状态自动计算）只改这里。
 */
@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    @Override
    public List<Activity> list() {
        return activityMapper.findAll();
    }

    @Override
    public Activity getById(Long id) {
        return activityMapper.findById(id);
    }

    @Override
    public void add(Activity activity) {
        // 新增时如果没传图片，用默认占位图，保证列表页图片不为空
        if (activity.getImage() == null || activity.getImage().isEmpty()) {
            activity.setImage("/images/activity-1.jpg");
        }
        activityMapper.insert(activity);
    }

    @Override
    public void update(Activity activity) {
        activityMapper.update(activity);
    }

    @Override
    public void delete(Long id) {
        activityMapper.deleteById(id);
    }
}
