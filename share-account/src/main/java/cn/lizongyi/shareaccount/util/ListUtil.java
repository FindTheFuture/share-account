package cn.lizongyi.shareaccount.util;

import cn.lizongyi.shareaccount.constants.NumConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * list工具类
 *
 * @author justin
 * @date 2023/02/22
 */
public class ListUtil {

    /**
     * 将list看成环型列表,从中取fetchNum个元素 
     * 返回取到的元素列表和最后一个元素索引位置
     *
     * @param originalList 原始列表
     * @param fetchNum 获取元素个数
     * @param lastIndex 上一次获取元素的索引位置
     * @return {@link List}<{@link CycleListResult}<{@link T}>>
     */
    public static <T> CycleListResult<T> fetchList(List<T> originalList, int fetchNum, int lastIndex) {
        if (originalList == null || originalList.isEmpty()) {
            return null;
        }
        int listSize = originalList.size();
        fetchNum = Math.max(0, Math.min(listSize - 1, fetchNum));
        // 以环形方式计算索引
        lastIndex = (lastIndex + 1 + listSize) % listSize;
        int startIndex = Math.min(listSize - 1, lastIndex + 1);
        List<T> subList = originalList.subList(startIndex, startIndex + fetchNum);
        int remainingElements = (startIndex + fetchNum + listSize - 1) % listSize - startIndex;
        remainingElements = Math.max(0, remainingElements);
        subList.addAll(originalList.subList(0, remainingElements));
        return new CycleListResult<>(subList, lastIndex);
    }

    /**
     * 合并并去重
     *
     * @param originList 原始列表
     * @param appendList 追加列表
     * @return 合并并去重后的列表
     */
    public static <T> List<T> mergeAndDistinct(List<T> originList, List<T> appendList) {
        List<T> resultList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(originList)) {
            resultList.addAll(originList);
        }
        if (!CollectionUtils.isEmpty(appendList)) {
            resultList.addAll(appendList);
        }
        return resultList.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 将list等分到多个子列表中
     *
     * @param list 原始列表
     * @param maxBuckets 最大子列表数量
     * @return 等分后的子列表列表
     */
    public static <T> List<List<T>> equalDistribute(List<T> list, int maxBuckets) {
        List<List<T>> groupLists = new ArrayList<>();
        for (int i = 0; i < maxBuckets; i++) {
            groupLists.add(new ArrayList<>());
        }

        int bucketIndex = NumConstant.INT0;
        for (T obj : list) {
            groupLists.get(bucketIndex).add(obj);
            bucketIndex = (bucketIndex + 1) % maxBuckets;
        }
        return groupLists.stream().filter(t -> !CollectionUtils.isEmpty(t)).collect(Collectors.toList());
    }



    /**
     * 将大列表按指定大小分割成多个小列表
     *
     * @param <T> 元素类型
     * @param list 原始列表
     * @param size 每个小列表的最大元素数量
     * @return 分割后的小列表集合
     */
    public static <T> List<List<T>> splitList(List<T> list, int size) {
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            result.add(new ArrayList<>(list.subList(i, Math.min(i + size, list.size()))));
        }
        return result;
    }



    /**
     * 列表拷贝
     *
     * @author lizongyi@itbox.cn
     * @date 2024/6/7
     * @param sourceList 来源
     * @param targetClass 目标
     * @return java.util.List<T>
     */
    public static <S, T> List<T> copyList(List<S> sourceList, Class<T> targetClass) {
        return sourceList.stream().map(source -> {
            try {
                T target = targetClass.getDeclaredConstructor().newInstance();
                BeanUtil.copy(source, target);
                return target;
            } catch (Exception e) {
                throw new RuntimeException("Copy list elements error", e);
            }
        }).collect(Collectors.toList());
    }


    /**
     * 将 字符串list转成Long list
     * @author lizongyi@itbox.cn
     * @date 2024/10/24
     * @param strList
     * @return java.util.List<java.lang.Long>
     */
    public static List<Long> convertToLongList(List<String> strList) {
        return strList.stream().map(Long::valueOf).collect(Collectors.toList());
    }


    public static List<String> convertToStringList(List<Long> longList) {
        return longList.stream().map(String::valueOf).collect(Collectors.toList());
    }

    public static List<Long> convertToLongList(String strList) {
        return Arrays.stream(strList.split(",")).map(Long::valueOf).collect(Collectors.toList());
    }




    /**
     * 循环列表
     *
     * @author justin
     * @date 2023/02/22
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CycleListResult<T> {

        /**
         * 结果列表
         */
        private List<T> resultList;

        /**
         * 最后一个元素索引位置
         */
        private int lastIndex;

    }
}