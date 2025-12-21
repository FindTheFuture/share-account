package cn.lizongyi.shareaccount.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 * ParameterizedType实现类：用于表示参数化类型。
 * 
 * @author 林淮川 linhuaichuan@itbox.cn
 * @date 1/3/2024
 */
public class ParameterizedTypeImpl implements ParameterizedType {
    /**
     * 实际类型参数
     */
    private final Type[] actualTypeArguments;

    /**
     * 拥有者类型
     */
    private final Type ownerType;

    /**
     * 原始类型
     */
    private final Type rawType;

    /**
     * 构造方法，用于创建ParameterizedTypeImpl对象。
     * 
     * @param actualTypeArguments 实际类型参数数组
     * @param ownerType 拥有者类型
     * @param rawType 原始类型
     */
    public ParameterizedTypeImpl(Type[] actualTypeArguments, Type ownerType, Type rawType) {
        this.actualTypeArguments = actualTypeArguments;
        this.ownerType = ownerType;
        this.rawType = rawType;
    }

    /**
     * 获取实际类型参数数组。
     */
    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    /**
     * 获取拥有者类型。
     */
    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    /**
     * 获取原始类型。
     */
    @Override
    public Type getRawType() {
        return rawType;
    }

    /**
     * 判断当前对象是否与指定对象相等。
     * 
     * @param o 要比较的对象
     * @return 如果相等则返回true，否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ParameterizedTypeImpl that = (ParameterizedTypeImpl)o;

        // 可能不正确 - 用Arrays.equals比较Object[]数组
        if (!Arrays.equals(actualTypeArguments, that.actualTypeArguments)) {
            return false;
        }
        if (!Objects.equals(ownerType, that.ownerType)) {
            return false;
        }
        return Objects.equals(rawType, that.rawType);

    }

    /**
     * 计算当前对象的哈希码。
     * 
     * @return 当前对象的哈希码
     */
    @Override
    public int hashCode() {
        int result = actualTypeArguments != null ? Arrays.hashCode(actualTypeArguments) : 0;
        result = 31 * result + (ownerType != null ? ownerType.hashCode() : 0);
        result = 31 * result + (rawType != null ? rawType.hashCode() : 0);
        return result;
    }
}