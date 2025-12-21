package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.AiTask;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI任务Mapper接口
 */
@Mapper
public interface AiTaskMapper {

    @Select("SELECT * FROM ai_task WHERE id = #{id}")
    AiTask findById(Long id);

    @Select("SELECT * FROM ai_task WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<AiTask> findByUserId(Long userId);

    @Select("SELECT * FROM ai_task WHERE request = #{objectId} ORDER BY create_time DESC")
    List<AiTask> findByObjectId(String objectId);

    @Insert("INSERT INTO ai_task (request, type, status, result, error_msg, retry_count, user_id, create_time, update_time, recognize_time, end_time) " +
            "VALUES (#{request}, #{type}, #{status}, #{result}, #{errorMsg}, #{retryCount}, #{userId}, #{createTime}, #{updateTime}, #{recognizeTime}, #{endTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiTask aiTask);

    @Update("UPDATE ai_task SET request = #{request}, type = #{type}, status = #{status}, result = #{result}, error_msg = #{errorMsg}, " +
            "retry_count = #{retryCount}, update_time = #{updateTime}, recognize_time = #{recognizeTime}, end_time = #{endTime} WHERE id = #{id}")
    int update(AiTask aiTask);

    @Update("UPDATE ai_task SET status = #{status}, result = #{result}, update_time = #{updateTime}, end_time = #{endTime} WHERE id = #{id}")
    int updateTaskStatus(@Param("id") Long id, @Param("status") Integer status, @Param("result") String result, 
                         @Param("updateTime") LocalDateTime updateTime, @Param("endTime") LocalDateTime endTime);

    @Update("UPDATE ai_task SET status = #{status}, error_msg = #{errorMsg}, retry_count = #{retryCount}, update_time = #{updateTime}, end_time = #{endTime} WHERE id = #{id}")
    int updateTaskError(@Param("id") Long id, @Param("status") Integer status, @Param("errorMsg") String errorMsg, 
                        @Param("retryCount") Integer retryCount, @Param("updateTime") LocalDateTime updateTime, 
                        @Param("endTime") LocalDateTime endTime);

    @Delete("DELETE FROM ai_task WHERE id = #{id}")
    int deleteById(Long id);

    @Update("UPDATE ai_task SET recognize_time = #{recognizeTime}, update_time = #{updateTime} WHERE id = #{id}")
    int updateRecognizeTime(@Param("id") Long id, @Param("recognizeTime") LocalDateTime recognizeTime, @Param("updateTime") LocalDateTime updateTime);
}