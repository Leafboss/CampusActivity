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

    /**
     * 按条件查询活动列表，按时间倒序（最近的活动排前面）。
     * keyword 为 null 不按关键词筛，status 为 null 不按状态筛；两个都不传就等于查全部。
     * SQL 写在同包同名的 ActivityMapper.xml 里 —— 动态 SQL 用 XML 的 <if> 比注解里拼字符串清楚得多。
     */
    List<Activity> findByCondition(@Param("keyword") String keyword, @Param("status") Integer status);

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

    /** 统计还有几条活动在用这张图（删除图片文件前确认没别的记录引用它） */
    @Select("SELECT COUNT(*) FROM activity WHERE image = #{image}")
    int countByImage(String image);
}
