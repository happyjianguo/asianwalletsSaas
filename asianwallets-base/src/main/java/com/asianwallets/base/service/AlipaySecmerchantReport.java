package com.asianwallets.base.service;

import com.asianwallets.common.entity.MerchantReport;
import com.asianwallets.common.utils.HttpResponse;

import java.io.IOException;

public interface AlipaySecmerchantReport {

    /**
     * 报备
     * @param merchantId 商户id
     * @param channelId 通道ID
     */
    void report(String merchantId, String channelId);

    /**
     * 重新报备
     * @param merchantReport 报备
     */
    void Resubmit(MerchantReport merchantReport);

    /**
     * 调用支付宝报备接口
     * @param merchantReport 报备
     * @return
     * @throws IOException
     */
    HttpResponse getHttpResponse(MerchantReport merchantReport) throws IOException;
}
