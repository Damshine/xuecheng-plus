package com.mrs.xuecheng.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mrs.xuecheng.base.exception.XueChengPlusException;
import com.mrs.xuecheng.base.utils.IdWorkerUtils;
import com.mrs.xuecheng.base.utils.QRCodeUtil;
import com.mrs.xuecheng.messagesdk.model.po.MqMessage;
import com.mrs.xuecheng.messagesdk.service.MqMessageService;
import com.mrs.xuecheng.orders.config.AlipayConfig;
import com.mrs.xuecheng.orders.config.PayNotifyConfig;
import com.mrs.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.mrs.xuecheng.orders.mapper.XcOrdersMapper;
import com.mrs.xuecheng.orders.mapper.XcPayRecordMapper;
import com.mrs.xuecheng.orders.model.dto.AddOrderDto;
import com.mrs.xuecheng.orders.model.dto.PayRecordDto;
import com.mrs.xuecheng.orders.model.dto.PayStatusDto;
import com.mrs.xuecheng.orders.model.po.XcOrders;
import com.mrs.xuecheng.orders.model.po.XcOrdersGoods;
import com.mrs.xuecheng.orders.model.po.XcPayRecord;
import com.mrs.xuecheng.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * description: OrderServiceImpl
 * date: 2023/6/2 16:10
 * author: MR.孙
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    XcOrdersMapper ordersMapper;

    @Autowired
    XcOrdersGoodsMapper ordersGoodsMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    XcPayRecordMapper payRecordMapper;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    OrderServiceImpl currentProxy;

    @Value("${pay.qrcodeurl}")
    String qrcodeurl;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;



    @Transactional
    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {

        // 1. 添加商品订单
        XcOrders xcOrders = saveXcOrders(userId, addOrderDto);
        // 2. 添加支付交易记录
        //插入支付记录
        XcPayRecord payRecord = createPayRecord(xcOrders);
        // 3. 生成二维码
        Long payNo = payRecord.getPayNo();
        //生成二维码
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        //支付二维码的url
        String url = String.format(qrcodeurl, payNo);
        //二维码图片
        String qrCode = null;


        try {
            // 3.1 用订单号填充占位符
            qrcodeurl = String.format(qrcodeurl, payRecord.getPayNo());
            // 3.2 生成二维码
            qrCode = new QRCodeUtil().createQRCode(qrcodeurl, 200, 200);
        } catch (IOException e) {
            XueChengPlusException.cast("生成二维码出错");
        }

        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);

        return payRecordDto;
    }

    @Override
    public XcPayRecord getPayRecordByPayNo(String payNo) {
        return payRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));    }

    @Override
    public PayRecordDto queryPayResult(String payNo) {
        //调用支付宝的接口查询支付结果
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        //拿到支付结果更新支付记录表和订单表的支付状态
        currentProxy.saveAliPayStatus(payStatusDto);

        //要返回最新的支付记录信息
        XcPayRecord payRecordByPayno = getPayRecordByPayno(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecordByPayno,payRecordDto);

        return payRecordDto;
    }


    /**
     *
     * @param payStatusDto
     */
    @Transactional
    @Override
    public void saveAliPayStatus(PayStatusDto payStatusDto) {
        //支付记录号
        String payNO = payStatusDto.getOut_trade_no();
        XcPayRecord payRecordByPayno = getPayRecordByPayno(payNO);
        if(payRecordByPayno == null){
            XueChengPlusException.cast("找不到相关的支付记录");
        }

        //拿到相关联的订单id
        Long orderId = payRecordByPayno.getOrderId();
        XcOrders xcOrders = ordersMapper.selectById(orderId);


        if(xcOrders == null){
            XueChengPlusException.cast("找不到相关联的订单");
        }

        //支付状态
        String statusFromDb = payRecordByPayno.getStatus();
        //如果数据库支付的状态已经是成功了，不再处理了
        if("601002".equals(statusFromDb)){
            return ;
        }

        //如果支付成功
        String trade_status = payStatusDto.getTrade_status();//从支付宝查询到的支付结果
        if(trade_status.equals("TRADE_SUCCESS")){//支付宝返回的信息为支付成功
            //更新支付记录表的状态为支付成功
            payRecordByPayno.setStatus("601002");
            //支付宝的订单号
            payRecordByPayno.setOutPayNo(payStatusDto.getTrade_no());
            //第三方支付渠道编号
            payRecordByPayno.setOutPayChannel("Alipay");
            //支付成功时间
            payRecordByPayno.setPaySuccessTime(LocalDateTime.now());
            payRecordMapper.updateById(payRecordByPayno);

            //更新订单表的状态为支付成功
            xcOrders.setStatus("600002");//订单状态为交易成功
            ordersMapper.updateById(xcOrders);

            //将消息写到数据库
            MqMessage mqMessage = mqMessageService.addMessage("payresult_notify", xcOrders.getOutBusinessId(), xcOrders.getOrderType(), null);
            //发送消息
            notifyPayResult(mqMessage);
        }


    }

    @Override
    public void notifyPayResult(MqMessage message) {

        //消息内容
        String jsonString = JSON.toJSONString(message);
        //消息持久化
        Message messageObj = MessageBuilder.withBody(jsonString.getBytes(StandardCharsets.UTF_8)).setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();


        //消息id【因为id是主键且自增所以唯一】
        Long id = message.getId();
        //全局消息Id
        CorrelationData correlationData = new CorrelationData(id.toString());

        //设置回调，判断消息是否成功到达交换机
        correlationData.getFuture().addCallback(result->{

            if(result.isAck()) {
                //消息成功发送到了交换机
                log.debug("发送消息成功:{}",jsonString);
                //将消息从数据库表mq_message删除
                mqMessageService.completed(id);
            } else {
                //消息发送失败
                log.debug("发送消息失败:{}",jsonString);
            }

        }, ex->{
            //发生异常了
            log.debug("发送消息异常:{}",jsonString);
        });


        //发送消息  路由键为空并且交换机为fanout表示发送所有与交换机绑定的队列
        rabbitTemplate.convertAndSend(PayNotifyConfig.PAYNOTIFY_EXCHANGE_FANOUT, "", messageObj, correlationData);


    }

    private XcPayRecord getPayRecordByPayno(String payNO) {
        XcPayRecord xcPayRecord = payRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNO));
        return xcPayRecord;
    }

    /**
     * 请求支付宝查询支付结果
     * @param payNo
     * @return
     */
    private PayStatusDto queryPayResultFromAlipay(String payNo) {
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, AlipayConfig.FORMAT, AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY,AlipayConfig.SIGNTYPE);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        //bizContent.put("trade_no", "2014112611001004680073956707");
        request.setBizContent(bizContent.toString());

        //支付宝返回的信息
        String body = null;
        try {
            //通过alipayClient调用API，获得对应的response类
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                //交易不成功
                XueChengPlusException.cast("请求支付宝查询支付结果失败");
            }
            body = response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
            XueChengPlusException.cast("请求支付查询支付结果异常");
        }

        Map bodyMap = JSON.parseObject(body, Map.class);
        Map alipay_trade_query_response = (Map) bodyMap.get("alipay_trade_query_response");

        //解析支付结果
        String trade_no = (String) alipay_trade_query_response.get("trade_no");
        String trade_status = (String) alipay_trade_query_response.get("trade_status");
        String total_amount = (String) alipay_trade_query_response.get("total_amount");
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);
        payStatusDto.setTrade_no(trade_no);//支付宝的交易号
        payStatusDto.setTrade_status(trade_status);//交易状态
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTotal_amount(total_amount);//总金额

        return payStatusDto;
    }


    /**
     * 保存支付记录
     * @param orders
     * @return
     */
    private XcPayRecord createPayRecord(XcOrders orders) {


        //订单id
        Long orderId = orders.getId();
        //判断订单是否存在
        XcOrders xcOrders = ordersMapper.selectById(orderId);
        //如果此订单不存在不能添加支付记录
        if (xcOrders == null) {
            XueChengPlusException.cast("订单不存在");
        }

        //订单状态
        String status = xcOrders.getStatus();

        //如果此订单支付结果为成功，不再添加支付记录，避免重复支付
        if ("601002".equals(status)) {//支付成功
            XueChengPlusException.cast("此订单已支付");
        }

        //添加支付记录
        XcPayRecord xcPayRecord = new XcPayRecord();
        xcPayRecord.setPayNo(IdWorkerUtils.getInstance().nextId());//支付记录号，将来要传给支付宝
        xcPayRecord.setOrderId(orderId);
        xcPayRecord.setOrderName(xcOrders.getOrderName());
        xcPayRecord.setTotalPrice(xcOrders.getTotalPrice());
        xcPayRecord.setCurrency("CNY");
        xcPayRecord.setCreateDate(LocalDateTime.now());
        xcPayRecord.setStatus("601001");//未支付
        xcPayRecord.setUserId(xcOrders.getUserId());
        int insert = payRecordMapper.insert(xcPayRecord);
        if(insert<=0){
            XueChengPlusException.cast("插入支付记录失败");
        }
        return xcPayRecord;

    }


    /**
     * 保存订单信息
     *
     * @param userId
     * @param addOrderDto
     * @return
     */
    private XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto) {
        //插入订单表,订单主表，订单明细表
        //进行幂等性判断，同一个选课记录只能有一个订单
        XcOrders xcOrders = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if (xcOrders != null) {
            return xcOrders;
        }

        xcOrders = new XcOrders();
        //使用雪花算法生成订单号
        xcOrders.setId(IdWorkerUtils.getInstance().nextId());
        xcOrders.setTotalPrice(addOrderDto.getTotalPrice());
        xcOrders.setCreateDate(LocalDateTime.now());
        xcOrders.setStatus("600001");//未支付
        xcOrders.setUserId(userId);
        xcOrders.setOrderType("60201");//订单类型
        xcOrders.setOrderName(addOrderDto.getOrderName());
        xcOrders.setOrderDescrip(addOrderDto.getOrderDescrip());
        xcOrders.setOrderDetail(addOrderDto.getOrderDetail());
        xcOrders.setOutBusinessId(addOrderDto.getOutBusinessId());//如果是选课这里记录选课表的id
        int insert = ordersMapper.insert(xcOrders);
        if(insert<=0){
            XueChengPlusException.cast("添加订单失败");
        }
        //插入订单明细表
        //订单id
        Long orderId = xcOrders.getId();
        String orderDetailJson = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoods = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
        //遍历xcOrdersGoods插入订单明细表

        xcOrdersGoods.forEach(goods->{
            goods.setOrderId(orderId);
            //插入订单明细表
            int insert1 = ordersGoodsMapper.insert(goods);
        });


        return xcOrders;
    }

    /**
     * 根据业务id查询订单 ,业务id是选课记录表中的主键
     * @param businessId
     * @return
     */
    private XcOrders getOrderByBusinessId(String businessId) {

        XcOrders xcOrders = ordersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, businessId));
        return xcOrders;
    }
}
