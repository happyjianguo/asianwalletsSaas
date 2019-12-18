package com.asianwallets.trade.service;

import java.math.BigDecimal;

/**
 * 通用业务接口
 */
public interface CommonBusinessService {

    /**
     * 校验MD5签名
     *
     * @param obj 验签对象
     * @return 布尔值
     */
    boolean checkSignByMd5(Object obj);

    /**
     * 校验重复请求【线上与线下下单】
     *
     * @param merchantId      商户编号
     * @param merchantOrderId 商户订单号
     * @return 布尔值
     */
    boolean repeatedRequests(String merchantId, String merchantOrderId);

    /**
     * 校验订单币种是否支持与默认值【线上与线下下单】
     *
     * @param orderCurrency 订单币种
     * @param orderAmount   订单金额
     * @return 布尔值
     */
    boolean checkOrderCurrency(String orderCurrency, BigDecimal orderAmount);
}