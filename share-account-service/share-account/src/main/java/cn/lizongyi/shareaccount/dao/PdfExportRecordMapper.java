package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.PdfExportRecord;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PdfExportRecordMapper {
    
    /**
     * 插入导出记录
     */
    @Insert("INSERT INTO pdf_export_record (user_id, request_params, status, file_name, file_url, created_at, completed_at, deleted_at) " +
            "VALUES (#{userId}, #{requestParams}, #{status}, #{fileName}, #{fileUrl}, #{createdAt}, #{completedAt}, #{deletedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PdfExportRecord record);
    
    /**
     * 更新导出记录
     */
    @Update("UPDATE pdf_export_record SET user_id = #{userId}, request_params = #{requestParams}, status = #{status}, " +
            "file_name = #{fileName}, file_url = #{fileUrl}, created_at = #{createdAt}, completed_at = #{completedAt}, " +
            "deleted_at = #{deletedAt} WHERE id = #{id}")
    int updateById(PdfExportRecord record);
    
    /**
     * 根据ID查询导出记录
     */
    @Select("SELECT * FROM pdf_export_record WHERE id = #{id}")
    PdfExportRecord selectById(Long id);
    
    /**
     * 查询用户在指定时间范围内的导出记录数量
     */
    @Select("SELECT COUNT(*) FROM pdf_export_record WHERE user_id = #{userId} AND created_at BETWEEN #{startTime} AND #{endTime}")
    int countByUserIdAndTimeRange(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询需要删除的过期记录（状态为成功且创建时间超过指定天数）
     */
    @Select("SELECT * FROM pdf_export_record WHERE status = #{status} AND created_at <= DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    List<PdfExportRecord> selectExpiredRecords(@Param("days") Integer days, @Param("status") Integer status);
    
    /**
     * 批量更新记录状态
     */
    @Update("<script>UPDATE pdf_export_record SET status = #{status}, deleted_at = #{deletedAt} WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status, @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * 更新单个记录状态
     */
    @Update("UPDATE pdf_export_record SET status = #{status}, deleted_at = #{deletedAt} WHERE id = #{id}")
    int updateDeleteStatus(@Param("id") Long id, @Param("status") Integer status, @Param("deletedAt") LocalDateTime deletedAt);
}