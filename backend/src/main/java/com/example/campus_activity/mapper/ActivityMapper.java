package com.example.campus_activity.mapper;

import com.example.campus_activity.pojo.Activity;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 活动表 Mapper：只写 SQL，不写业务逻辑。
 * 本项目 SQL 都比较简单，统一用注解方式（复杂 SQL 才考虑 XML）。
 */
@Mapper
public interface ActivityMapper {

    /** 查询全部活动，按时间倒序（最近的活动排前面） */
    @Select("SELECT * FROM activity ORDER BY time DESC")
    List<Activity> findAll();

    /** 按 id 查询单个活动 */
    @Select("SELECT * FROM activity WHERE id = #{id}")
    Activity findById(Long id);

    /** 新增活动，useGeneratedKeys 把自增 id 回填到实体里（新增后前端能立刻拿到 id 跳详情） */
    @Insert("INSERT INTO activity(name, time, location, summary, detail, image, status) " +
            "VALUES(#{name}, #{time}, #{location}, #{summary}, #{detail}, #{image}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Activity activity);

    /** 按 id 全字段更新 */
    @Update("UPDATE activity SET name=#{name}, time=#{time}, location=#{location}, " +
            "summary=#{summary}, detail=#{detail}, image=#{image}, status=#{status} WHERE id=#{id}")
    void update(Activity activity);

    /** 按 id 删除 */
    @Delete("DELETE FROM activity WHERE id = #{id}")
    void deleteById(Long id);
}
