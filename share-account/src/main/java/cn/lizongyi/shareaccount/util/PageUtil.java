package cn.lizongyi.shareaccount.util;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 分页工具类
 * 
 * @author justin
 * @date 2022/6/8 14:34
 */
public class PageUtil {

    /**
     * 将列表根据分页分成多个列表
     *
     * @param originalList 源列表
     * @param pageSize 分页大小
     * @param <T> 泛型
     * @return 返回值
     */
    public static <T> List<List<T>> splitListByPage(List<T> originalList, int pageSize) {
        if (CollectionUtils.isEmpty(originalList)) {
            return Collections.emptyList();
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize 必须大于0");
        }

        List<List<T>> returnList = new ArrayList<>();
        while (!originalList.isEmpty()) {
            int currentPageEndIndex = Math.min(originalList.size(), pageSize);
            returnList.add(originalList.subList(0, currentPageEndIndex));
            originalList = originalList.subList(currentPageEndIndex, originalList.size());
        }
        return returnList;
    }
}