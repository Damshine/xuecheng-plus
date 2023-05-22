package com.mrs.xuecheng.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mrs.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    @Select("SELECT * FROM media_process WHERE id % #{shardTotal} = #{shardIndex} AND ( status = '1' or status = '3' ) AND fail_count < 3 LIMIT #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardTotal") int shardTotal, @Param("shardIndex") int shardIndex, @Param("count") int count);

    /**
     * 使用乐观锁实现分布式锁，避免重复执行任务
     * @param id
     * @return
     */
    @Update("UPDATE media_process m SET m.status = '4'  WHERE m.id = #{id} AND (m.status = '1' or m.status = '3') AND m.fail_count < 3")
    int startTask(@Param("id") long id);

}
