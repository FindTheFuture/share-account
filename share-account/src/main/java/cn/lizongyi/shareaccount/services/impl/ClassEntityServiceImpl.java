package cn.lizongyi.shareaccount.services.impl;

import cn.lizongyi.shareaccount.dao.ClassEntityMapper;
import cn.lizongyi.shareaccount.entity.ClassEntity;
import cn.lizongyi.shareaccount.enums.ClassStatusEnum;
import cn.lizongyi.shareaccount.request.CreateClassRequest;
import cn.lizongyi.shareaccount.response.ClassResponse;
import cn.lizongyi.shareaccount.services.BaseHandler;
import cn.lizongyi.shareaccount.services.ClassEntityService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassEntity 对应的 Service 实现类
 */
@Service
public class ClassEntityServiceImpl implements ClassEntityService {

    @Resource
    private ClassEntityMapper classEntityMapper;
    @Autowired
    private BaseHandler baseHandler;

    @Override
    public ClassResponse selectById(Long id) {
        ClassEntity classEntity = classEntityMapper.selectById(id);
        return ClassResponse.format(classEntity);
    }

    @Override
    public List<ClassResponse> selectAll() {
        List<ClassEntity> classEntities = classEntityMapper.selectAll();
        return classEntities.stream()
                .map(ClassResponse::format)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean save(CreateClassRequest classRequest) {
        ClassEntity classEntity = new ClassEntity();
        if(classRequest.getId() != null && classRequest.getId() != 0) {
            classEntity = classEntityMapper.selectById(classRequest.getId());
        }

        classEntity.setUserId(baseHandler.getUserId());
        classEntity.setName(classRequest.getName());
        classEntity.setIcon(classRequest.getIcon());
        classEntity.setParentId(classRequest.getParentId());
        classEntity.setType(2);     // 暂时表示最后一级分类

        boolean success = false;
        if(classRequest.getId() == null || classRequest.getId() == 0) {
            classEntity.setStatus(ClassStatusEnum.NORMAL.getId());
            classEntity.setCreateTime(LocalDateTime.now());
            success = classEntityMapper.insert(classEntity) > 0;
        } else {

            classEntity.setStatus(classRequest.getStatus());
            success = classEntityMapper.updateById(classEntity) > 0;
        }
        return success;
    }


    /**
     * 更新状态
     * @param id
     * @return
     */
    @Override
    public Boolean updateStatus(Long id) {
        if(id == null || id == 0L) {
            return false;
        }

        ClassEntity classEntity = classEntityMapper.selectById(id);
        if(classEntity == null) {
            return false;
        }

        int rows = classEntityMapper.updateStatus(id, ClassStatusEnum.NORMAL.getId() == classEntity.getStatus() ? ClassStatusEnum.DELETE.getId() : ClassStatusEnum.NORMAL.getId());
        return rows > 0;
    }

    @Override
    public boolean deleteById(Long id) {

        int rowsAffected = classEntityMapper.deleteById(id);
        return rowsAffected > 0;
    }

    @Override
    public List<ClassResponse> selectByUserId(Long userId) {
        if(userId == null) {
            return null;
        }
        List<ClassEntity> allMainList = classEntityMapper.selectAllMain();

        if(CollectionUtils.isNotEmpty(allMainList)) {
            List<ClassEntity> list = classEntityMapper.selectByUserId(userId);
            if(CollectionUtils.isNotEmpty(list)) {
                allMainList.addAll(list);
            }
        }

        // 树形结构
        return ClassResponse.buildTree(allMainList);
    }


    @Override
    public List<ClassEntity> selectByUserIdAndType(boolean selectMain, Long userId, Integer type) {
        if(userId == null || type == null) {
            return null;
        }
        List<ClassEntity> allMainList = new ArrayList<>();

        List<ClassEntity> list = classEntityMapper.selectByTypeAndNoUser(type);
        if(CollectionUtils.isNotEmpty(list)) {
            allMainList.addAll(list);
        }
        list = classEntityMapper.selectByUserIdAndType(userId, type);
        if(CollectionUtils.isNotEmpty(list)) {
            allMainList.addAll(list);
        }
        return allMainList;
    }

    @Override
    public List<ClassResponse> selectByParentId(Long parentId) {
        if(parentId == null) {
            return null;
        }
        Long userId = baseHandler.getUserId();

        List<ClassEntity> allMainList = classEntityMapper.selectAllMainByParentId(parentId);

        if(CollectionUtils.isNotEmpty(allMainList)) {
            List<ClassEntity> list = classEntityMapper.selectByParentId(parentId, userId);
            if(CollectionUtils.isNotEmpty(list)) {
                allMainList.addAll(list);
            }
        }
        // 树形结构
        return ClassResponse.buildTree(allMainList);
    }

    @Override
    public List<ClassResponse> selectByStatus(Integer status) {
        if(status == null) {
            return null;
        }

        Long userId = baseHandler.getUserId();
        List<ClassEntity> list = classEntityMapper.selectByUserIdAndStatus(userId, status);
        return list.stream()
                .map(ClassResponse::format)
                .collect(Collectors.toList());

    }


    @Override
    public List<ClassEntity> selectByUserIdAndStatus(boolean selectMain, Long userId, Integer status) {
        if(status == null) {
            return null;
        }

        List<ClassEntity> allMainList = new ArrayList<>();
        if(selectMain) {
            allMainList.addAll(classEntityMapper.selectAllMain());
        }

        List<ClassEntity> list = classEntityMapper.selectByUserIdAndStatus(userId, status);
        if(CollectionUtils.isNotEmpty(list)) {
            allMainList.addAll(list);
        }
        return allMainList;
    }

    /**
     * 获取最顶层分类ID
     * 递归查找给定分类的最顶层父分类
     * @param classId 分类ID
     * @return 最顶层分类ID
     */
    @Override
    public Long getTopClassId(Long classId) {
        if (classId == null || classId <= 0) {
            return null;
        }

        // 查询当前分类
        ClassEntity currentClass = classEntityMapper.selectById(classId);
        Assert.notNull(currentClass, "分类不存在: " + classId);

        // 如果当前分类已经是顶级分类（type=0），直接返回ID
        if (currentClass.getType() == 0) {
            return currentClass.getId();
        }

        // 如果有父分类，递归查找父分类的顶级分类
        if (currentClass.getParentId() != null && currentClass.getParentId() > 0) {
            return getTopClassId(currentClass.getParentId());
        }

        // 如果没有父分类但不是顶级分类（不应该出现这种情况），返回自身ID
        return currentClass.getId();
    }


    /**
     * 根据分类id查询所有子分类id，递归查询所有子分类
     */
    @Override
    public List<Long> selectAllChildIds(Long classId) {
        if(classId == null || classId <= 0) {
            return null;
        }
        List<Long> childIds = classEntityMapper.selectAllChildIds(classId);
        if(CollectionUtils.isNotEmpty(childIds)) {
            // 创建临时列表存储递归结果，避免并发修改异常
            List<Long> allChildIds = new ArrayList<>(childIds);
            for(Long childId : childIds) {
                List<Long> subChildIds = selectAllChildIds(childId);
                if(CollectionUtils.isNotEmpty(subChildIds)) {
                    allChildIds.addAll(subChildIds);
                }
            }
            return allChildIds;
        }
        return childIds;
    }
}
