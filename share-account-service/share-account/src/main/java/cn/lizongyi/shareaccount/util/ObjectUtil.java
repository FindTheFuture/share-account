package cn.lizongyi.shareaccount.util;

import java.util.Arrays;
import java.util.Objects;

/**
 * object判空
 *
 * @author justin
 * @date 2023/03/30
 */
public class ObjectUtil {

    /**
     * 所有非空
     *
     * @param objs 目标对象
     */
    public static boolean allNotNull(Object... objs) {
        return Arrays.stream(objs).allMatch(Objects::nonNull);
    }

    /**
     * 所有都空
     *
     * @param objs 目标对象
     */
    public static boolean allNull(Object... objs) {
        return Arrays.stream(objs).allMatch(Objects::isNull);
    }

    /**
     * 存在空
     *
     * @param objs 目标对象
     */
    public static boolean existNull(Object... objs) {
        return Arrays.stream(objs).anyMatch(Objects::isNull);
    }
}