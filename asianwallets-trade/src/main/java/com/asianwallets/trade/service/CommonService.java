package com.asianwallets.trade.service;

import com.asianwallets.common.entity.Orders;
import com.asianwallets.common.response.BaseResponse;
import com.asianwallets.common.dto.ArtificialDTO;

/**
 * 共通模块
 */
public interface CommonService {

    /**
     *校验密码
     * @param oldPassword
     * @param password
     * @return
     */
    Boolean checkPassword(String oldPassword, String password);


    /**
     * 人工回调
     *
     * @param artificialDTO artificialDTO
     * @return
     */
    BaseResponse artificialCallback(ArtificialDTO artificialDTO);

    /**
     * 订单成功后上报清结算
     * @param orders
     */
    void fundChangePlaceOrderSuccess(Orders orders);
}
