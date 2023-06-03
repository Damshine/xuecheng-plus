package com.mrs.xuecheng.orders.service;

import com.mrs.xuecheng.orders.model.dto.AddOrderDto;
import com.mrs.xuecheng.orders.model.dto.PayRecordDto;
import com.mrs.xuecheng.orders.model.dto.PayStatusDto;
import com.mrs.xuecheng.orders.model.po.XcPayRecord;

public interface OrderService {

    /**
     *  创建商品订单
     * @param userId
     * @param addOrderDto
     * @return
     */
    PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);


    /**
     * 查询支付交易记录
     * @param payNo 交易记录号
     * @return
     */
    XcPayRecord getPayRecordByPayNo(String payNo);


    /**
     * 请求支付宝查询支付结果
     * @param payNo 支付记录id
     * @return  支付记录信息
     */
    PayRecordDto queryPayResult(String payNo);


    /**
     * 保存支付状态
     * @param payStatusDto
     */
    void saveAliPayStatus(PayStatusDto payStatusDto);
}
