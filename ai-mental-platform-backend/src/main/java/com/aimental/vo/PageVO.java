package com.aimental.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 通用分页返回视图对象
 *
 * @param <T> 列表元素类型
 */
public class PageVO<T> {

    /**
     * 当前页数据列表
     */
    private List<T> list;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long pageNum;

    /**
     * 每页条数
     */
    private Long pageSize;

    public PageVO() {
    }

    /**
     * 构造方法
     *
     * @param list     当前页数据
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     */
    public PageVO(List<T> list, Long total, Long pageNum, Long pageSize) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    /**
     * 从MyBatis-Plus分页对象构建PageVO
     *
     * @param pager    分页对象
     * @param list     当前页转换后的数据
     * @param <T>      列表元素类型
     * @return PageVO
     */
    public static <T> PageVO<T> of(IPage<?> pager, List<T> list) {
        return new PageVO<>(list, pager.getTotal(), pager.getCurrent(), pager.getSize());
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getPageNum() {
        return pageNum;
    }

    public void setPageNum(Long pageNum) {
        this.pageNum = pageNum;
    }

    public Long getPageSize() {
        return pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }
}