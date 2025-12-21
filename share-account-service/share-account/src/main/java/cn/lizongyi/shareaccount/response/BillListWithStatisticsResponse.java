package cn.lizongyi.shareaccount.response;

import com.github.pagehelper.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 带统计信息的账单列表响应类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BillListWithStatisticsResponse<T> extends PageInfo<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 总收入金额（分）
     */
    private Long totalIncome = 0L;

    /**
     * 总支出金额（分）
     */
    private Long totalExpense = 0L;

    /**
     * 无参构造函数
     */
    public BillListWithStatisticsResponse() {
        super();
    }

    /**
     * 从PageInfo构造
     * @param pageInfo 原始分页信息
     */
    public BillListWithStatisticsResponse(PageInfo<T> pageInfo) {
        super(pageInfo.getList());
        this.setTotal(pageInfo.getTotal());
        this.setPageNum(pageInfo.getPageNum());
        this.setPageSize(pageInfo.getPageSize());
        this.setPages(pageInfo.getPages());
        this.setSize(pageInfo.getSize());
        this.setStartRow(pageInfo.getStartRow());
        this.setEndRow(pageInfo.getEndRow());
        this.setPrePage(pageInfo.getPrePage());
        this.setNextPage(pageInfo.getNextPage());
        this.setIsFirstPage(pageInfo.isIsFirstPage());
        this.setIsLastPage(pageInfo.isIsLastPage());
        this.setHasPreviousPage(pageInfo.isHasPreviousPage());
        this.setHasNextPage(pageInfo.isHasNextPage());
        this.setNavigatePages(pageInfo.getNavigatePages());
        this.setNavigatepageNums(pageInfo.getNavigatepageNums());
        this.setNavigateFirstPage(pageInfo.getNavigateFirstPage());
        this.setNavigateLastPage(pageInfo.getNavigateLastPage());
    }
}