package com.mrs.xuecheng.base.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description: PageParams
 * date: 2023/4/12 23:02
 * author: MR.孙
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageParams {

    //当前页码默认值
    public static final long DEFAULT_PAGE_CURRENT = 1L;
    //每页记录数默认值
    public static final long DEFAULT_PAGE_SIZE = 10L;
    //当前页码
    private Long pageNo = DEFAULT_PAGE_CURRENT;
    //每页记录数默认值
    private Long pageSize = DEFAULT_PAGE_SIZE;





}
