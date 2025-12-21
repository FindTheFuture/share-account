package cn.lizongyi.shareaccount.services;

import cn.lizongyi.shareaccount.entity.ClassEntity;
import cn.lizongyi.shareaccount.request.CreateClassRequest;
import cn.lizongyi.shareaccount.response.ClassResponse;

import java.util.List;

/**
 * ClassEntity 对应的 Service 接口
 */
public interface ClassEntityService {

    /**
     * 根据 ID 查询分类响应对象
     * @param id 分类的 ID
     * @return 分类响应对象，如果未找到则返回 null
     */
    ClassResponse selectById(Long id);

    /**
     * 查询所有分类响应对象列表
     * @return 分类响应对象列表
     */
    List<ClassResponse> selectAll();

    /**
     * 插入分类，返回插入后的分类响应对象
     * @param classResponse 要插入的分类响应对象
     * @return 插入成功后的分类响应对象
     */
    Boolean save(CreateClassRequest classRequest);

    /**
     * 根据 ID 更新分类状态
     * @param id 要更新的分类的 ID
     * @param status 新的分类状态
     * @return 更新成功返回 true，失败返回 false
     */
    Boolean updateStatus(Long id);


    /**
     * 根据 ID 删除分类，返回删除结果
     * @param id 要删除的分类的 ID
     * @return 删除成功返回 true，失败返回 false
     */
    boolean deleteById(Long id);

    /**
     * 根据用户 ID 查询分类响应对象列表
     * @param userId 用户 ID
     * @return 分类响应对象列表
     */
    List<ClassResponse> selectByUserId(Long userId);

    /**
     * 根据用户 ID 和分类类型查询分类响应对象列表
     * @param userId 用户 ID
     * @param type 分类类型
     * @return 分类响应对象列表
     */
    List<ClassEntity> selectByUserIdAndType(boolean selectMain, Long userId, Integer type);

    /**
     * 根据 parentId 查询下一层所有分类响应对象列表
     * @param parentId 父级分类 ID
     * @return 下一层分类响应对象列表
     */
    List<ClassResponse> selectByParentId(Long parentId);


    /**
     * 根据状态查询分类响应对象列表
     * @param status 分类状态
     * @return 分类响应对象列表
     */
    List<ClassResponse> selectByUserIdAndStatus(Integer status);

    /**
     * 获取最顶层分类ID
     * 递归查找给定分类的最顶层父分类
     * @param classId 分类ID
     * @return 最顶层分类ID
     */
    Long getTopClassId(Long classId);



    /**
     * 根据分类id查询所有子分类id，递归查询所有子分类
     */
    List<Long> selectAllChildIds(Long classId);
}