package cn.lizongyi.shareaccount.response;

import cn.lizongyi.shareaccount.entity.ClassEntity;
import cn.lizongyi.shareaccount.enums.ClassStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassResponse {

    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类图标
     */
    private String icon;

    /**
     * 父级id，用于关联上一级分类，顶级分类的父级id为 null
     */
    private Long parentId;

    /**
     * 0、顶级 1、一级分类 2、二级分类 3、三级分类 4、四级分类
     */
    private Integer type;

    /**
     * 0、正常 1、删除
     */
    private Integer status = 0;
    private String statusName;


    /**
     * 创建时间
     */
    //private String createTime;

    /**
     * 子分类列表，存储当前分类下的所有子分类
     */
    private List<ClassResponse> childClassList;


    /**
     * 格式化
     * @param classEntity
     * @return
     */
    public static ClassResponse format(ClassEntity classEntity) {

        if(classEntity == null) {
            return null;
        }

        ClassResponse response = new ClassResponse();
        response.setId(classEntity.getId());
        response.setUserId(classEntity.getUserId());
        response.setName(classEntity.getName());
        response.setIcon(classEntity.getIcon());
        response.setParentId(classEntity.getParentId());
        response.setType(classEntity.getType());
        response.setStatus(classEntity.getStatus());
        response.setStatusName(ClassStatusEnum.fromId(classEntity.getStatus()).getName());
        //response.setCreateTime(DateUtil.localDateTimeToString(classEntity.getCreateTime()));
        return response;
    }

    /**
     * 递归构建多层级分类结构
     * @param classEntities 所有分类实体列表
     * @return 包含完整多层级结构的分类响应列表
     */
    public static List<ClassResponse> buildTree(List<ClassEntity> classEntities) {
        // 先将 ClassEntity 转换为 ClassResponse
        List<ClassResponse> responses = classEntities.stream()
                .map(ClassResponse::format)
                .collect(Collectors.toList());

        // 使用 Map 存储每个分类的子分类列表，键为分类的 id，值为子分类列表
        Map<Long, List<ClassResponse>> childMap = new HashMap<>();
        for (ClassResponse response : responses) {
            Long parentId = response.getParentId();
            childMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(response);
        }

        // 找出顶级分类，即 parentId 为 null 的分类
        List<ClassResponse> rootNodes = childMap.getOrDefault(null, new ArrayList<>());
        if(CollectionUtils.isEmpty(rootNodes)) {
            return responses;
        }

        // 递归构建子分类，添加循环检测集合
        Set<Long> processedIds = new HashSet<>();
        for (ClassResponse root : rootNodes) {
            buildChildren(root, childMap, processedIds);
        }

        return rootNodes;
    }

    /**
     * 递归构建子分类，添加循环检测防止栈溢出
     * @param parent 父分类
     * @param childMap 存储每个分类的子分类列表的 Map
     * @param processedIds 已处理分类ID集合，用于检测循环引用
     */
    private static void buildChildren(ClassResponse parent, Map<Long, List<ClassResponse>> childMap, Set<Long> processedIds) {
        // 检查是否已处理过当前分类，防止循环引用
        if (processedIds.contains(parent.getId())) {
            return; // 遇到循环引用，直接返回
        }
        
        // 将当前分类ID加入已处理集合
        processedIds.add(parent.getId());
        
        List<ClassResponse> children = childMap.get(parent.getId());
        if (children != null && !children.isEmpty()) {
            parent.setChildClassList(children);
            for (ClassResponse child : children) {
                // 递归构建子分类，传递已处理集合
                buildChildren(child, childMap, processedIds);
            }
        }
    }

}
