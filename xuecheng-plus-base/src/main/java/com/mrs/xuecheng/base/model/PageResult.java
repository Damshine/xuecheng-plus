package com.mrs.xuecheng.base.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * description: PageResult
 * date: 2023/4/13 9:11
 * author: MR.孙
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageResult<T> implements Serializable {

    /**
     * 数据列表
     */
    private List<T> items;

    /**
     * 总记录数
     */
    private long counts;

    /**
     * 当前页码
     */
    private long page;

    /**
     * 每页记录数
     */
    private long pageSize;






}
