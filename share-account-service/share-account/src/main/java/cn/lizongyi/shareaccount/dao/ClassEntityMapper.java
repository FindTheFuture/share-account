package cn.lizongyi.shareaccount.dao;

import cn.lizongyi.shareaccount.entity.ClassEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * ClassEntity 对应的 MyBatis Mapper 接口
 */
@Mapper
public interface ClassEntityMapper {

    /**
     * 根据 ID 查询分类实体
     * @param id 分类实体的 ID
     * @return 分类实体对象，如果未找到则返回 null
     */
    @Select("SELECT * FROM `class` WHERE id = #{id} ORDER BY sort IS NULL, sort ASC, id ASC")
    ClassEntity selectById(@Param("id") Long id);

    @Select("SELECT * FROM `class` WHERE id = #{id} AND status = #{status} ORDER BY sort IS NULL, sort ASC, id ASC")
    ClassEntity selectByIdAndStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 查询所有分类实体
     * @return 分类实体列表
     */
    @Select("SELECT * FROM `class` WHERE status = 0 ORDER BY sort IS NULL, sort ASC, id ASC")
    List<ClassEntity> selectAll();

    /**
     * 查询所有原始的分类实体
     * @return 分类实体列表
     */
    @Select("SELECT * FROM `class` WHERE status = 0 and user_id is null ORDER BY sort IS NULL, sort ASC, id ASC")
    List<ClassEntity> selectAllMain();

    @Select("SELECT * FROM `class` WHERE parent_id = #{parentId} and status = 0 and user_id is null ORDER BY sort IS NULL, sort ASC, id ASC")
    List<ClassEntity> selectAllMainByParentId(@Param("parentId") Long parentId);

    /**
     * 插入分类实体
     * @param classEntity 要插入的分类实体对象
     * @return 插入成功的记录数
     */
  @Insert("INSERT INTO `class` (user_id, name, icon, parent_id, type, status, create_time) " +
            "VALUES (#{classEntity.userId}, #{classEntity.name}, #{classEntity.icon}, #{classEntity.parentId}, " +
            "#{classEntity.type}, #{classEntity.status}, #{classEntity.createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "classEntity.id", keyColumn = "id")
    int insert(@Param("classEntity") ClassEntity classEntity);

    /**
     * 根据 ID 更新分类实体
     * @param classEntity 要更新的分类实体对象，需包含 ID
     * @return 更新成功的记录数
     */
    @Update("UPDATE `class` SET user_id = #{classEntity.userId}, name = #{classEntity.name}, " +
            "icon = #{classEntity.icon}, parent_id = #{classEntity.parentId}, type = #{classEntity.type}, " +
            "status = #{classEntity.status}, create_time = #{classEntity.createTime} " +
            "WHERE id = #{classEntity.id}")
    int updateById(@Param("classEntity") ClassEntity classEntity);

    @Update("UPDATE `class` SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 根据 ID 删除分类实体
     * @param id 要删除的分类实体的 ID
     * @return 删除成功的记录数
     */
    @Update("UPDATE `class` SET status = 1 WHERE id = #{id} AND status = 0")
    int deleteById(@Param("id") Long id);

    /**
     * 根据用户 ID 查询分类实体
     * @param userId 用户 ID
     * @return 分类实体列表
     */
    @Select("SELECT * FROM `class` WHERE user_id = #{userId} AND status = 0 ORDER BY sort IS NULL, sort ASC, id ASC")
    List<ClassEntity> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM `class` WHERE user_id = #{userId} AND type = #{type} AND status = 0 ORDER BY sort IS NULL, sort ASC, id ASC")
    List<ClassEntity> selectByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type);

    @Select("SELECT * FROM `class` WHERE user_id is null AND type = #{type} AND status = 0 ORDER BY sort IS NULL, sort ASC, id ASC")
    List<ClassEntity> selectByTypeAndNoUser(@Param("type") Integer type);

    /**
     * 根据 parentId 查询下一层所有分类实体列表
     * @param parentId 父级分类 ID
     * @return 下一层分类实体列表
     */
    @Select("SELECT * FROM `class` WHERE parent_id = #{parentId} and user_id = #{userId} AND status = 0 ORDER BY sort IS NULL, sort ASC, id ASC")
    List<ClassEntity> selectByParentId(@Param("parentId") Long parentId, @Param("userId") Long userId);

    /**
     * 根据 用户ID 和状态查询分类实体
     * @param userId 用户 ID
     * @param status 状态
     * @return 分类实体列表
     */
    @Select("SELECT * FROM `class` WHERE user_id = #{userId} AND status = #{status} ORDER BY sort IS NULL, sort ASC, id ASC")
    List<ClassEntity> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);


    /**
     * 根据分类id查询所有子分类id
     */
    @Select("SELECT id FROM `class` WHERE parent_id = #{classId} AND status = 0 ORDER BY sort IS NULL, sort ASC, id ASC")
    List<Long> selectAllChildIds(@Param("classId") Long classId);

}