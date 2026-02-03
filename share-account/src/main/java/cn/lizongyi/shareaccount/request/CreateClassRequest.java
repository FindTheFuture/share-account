package cn.lizongyi.shareaccount.request;

import lombok.Data;

@Data
public class CreateClassRequest {

    private Long id;

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


}
