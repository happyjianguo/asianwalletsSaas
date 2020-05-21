package com.asianwallets.trade.channels.th.impl;

import com.alibaba.fastjson.JSON;
import com.asianwallets.common.constant.AD3Constant;
import com.asianwallets.common.constant.AD3MQConstant;
import com.asianwallets.common.constant.AsianWalletConstant;
import com.asianwallets.common.constant.TradeConstant;
import com.asianwallets.common.dto.RabbitMassage;
import com.asianwallets.common.dto.th.ISO8583.ISO8583DTO;
import com.asianwallets.common.dto.th.ISO8583.NumberStringUtil;
import com.asianwallets.common.dto.th.ISO8583.ThDTO;
import com.asianwallets.common.dto.th.ISO8583.TlvUtil;
import com.asianwallets.common.entity.*;
import com.asianwallets.common.exception.BusinessException;
import com.asianwallets.common.response.BaseResponse;
import com.asianwallets.common.response.EResultEnum;
import com.asianwallets.common.vo.clearing.FundChangeDTO;
import com.asianwallets.trade.channels.ChannelsAbstractAdapter;
import com.asianwallets.trade.channels.th.ThService;
import com.asianwallets.trade.dao.ChannelsOrderMapper;
import com.asianwallets.trade.dao.OrderRefundMapper;
import com.asianwallets.trade.dao.OrdersMapper;
import com.asianwallets.trade.dao.ReconciliationMapper;
import com.asianwallets.trade.feign.ChannelsFeign;
import com.asianwallets.trade.rabbitmq.RabbitMQSender;
import com.asianwallets.trade.service.ClearingService;
import com.asianwallets.trade.service.CommonBusinessService;
import com.asianwallets.trade.utils.HandlerType;
import com.payneteasy.tlv.BerTag;
import com.payneteasy.tlv.BerTlvBuilder;
import com.payneteasy.tlv.HexUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;

@Slf4j
@Service
@HandlerType(TradeConstant.TH)
public class ThServiceImpl extends ChannelsAbstractAdapter implements ThService {

    @Autowired
    private ChannelsFeign channelsFeign;

    @Autowired
    private ChannelsOrderMapper channelsOrderMapper;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private OrderRefundMapper orderRefundMapper;

    @Autowired
    private CommonBusinessService commonBusinessService;

    @Autowired
    private ReconciliationMapper reconciliationMapper;

    @Autowired
    private ClearingService clearingService;

    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 通华主扫接口
     *
     * @param orders  订单
     * @param channel 通道
     * @return BaseResponse
     */
    @Override
    public BaseResponse offlineCSB(Orders orders, Channel channel) {
        ChannelsOrder channelsOrder = new ChannelsOrder();
        channelsOrder.setId(orders.getId());
        channelsOrder.setMerchantOrderId(orders.getMerchantOrderId());
        channelsOrder.setTradeCurrency(orders.getTradeCurrency());
        channelsOrder.setTradeAmount(orders.getTradeAmount());
        channelsOrder.setReqIp(orders.getReqIp());
        channelsOrder.setServerUrl(orders.getServerUrl());
        channelsOrder.setTradeStatus(TradeConstant.TRADE_WAIT);
        channelsOrder.setIssuerId(orders.getIssuerId());
        channelsOrder.setOrderType(AD3Constant.TRADE_ORDER);
        channelsOrder.setMd5KeyStr(channel.getMd5KeyStr());
        channelsOrder.setPayerPhone(orders.getPayerPhone());
        channelsOrder.setPayerName(orders.getPayerName());
        channelsOrder.setPayerBank(orders.getPayerBank());
        channelsOrder.setPayerEmail(orders.getPayerEmail());
        channelsOrder.setCreateTime(new Date());
        channelsOrder.setCreator(orders.getCreator());
        channelsOrderMapper.insert(channelsOrder);

        ISO8583DTO iso8583DTO = new ISO8583DTO();
        //消息类型
        iso8583DTO.setMessageType("0200");
        //交易处理码
        iso8583DTO.setProcessingCode_3("700200");
        //获取交易金额的小数位数
        int numOfBits = String.valueOf(orders.getTradeAmount()).length() - String.valueOf(orders.getTradeAmount()).indexOf(".") - 1;
        int tradeAmount = 0;
        if (numOfBits == 0) {
            //整数
            tradeAmount = orders.getTradeAmount().intValue();
        } else {
            //小数,扩大对应小数位数
            tradeAmount = orders.getTradeAmount().movePointRight(numOfBits).intValue();
        }
        //12位,左边填充0
        String formatAmount = String.format("%012d", tradeAmount);
        //交易金额
        iso8583DTO.setAmountOfTransactions_4(formatAmount);
        //当前时间戳
        String timeStamp = System.currentTimeMillis() + "";
        String domain11 = timeStamp.substring(0, 6);
        String domain60_2 = timeStamp.substring(6, 12);
        //受卡方系统跟踪号
        iso8583DTO.setSystemTraceAuditNumber_11(domain11);
        //服务点输入方式码
        iso8583DTO.setPointOfServiceEntryMode_22("030");
        //服务点条件码
        iso8583DTO.setPointOfServiceConditionMode_25("00");
        //受理方标识码 (机构号)
        iso8583DTO.setAcquiringInstitutionIdentificationCode_32(channel.getExtend2());
        //受卡机终端标识码 (设备号)
        iso8583DTO.setCardAcceptorTerminalIdentification_41(channel.getExtend1());
        //受卡方标识码 (商户号)
        iso8583DTO.setCardAcceptorIdentificationCode_42(channel.getChannelMerchantId());
        iso8583DTO.setAdditionalData_46(TlvUtil.tlv5f52("303002" + channel.getPayCode() + "0202"));
        //交易货币代码
        iso8583DTO.setCurrencyCodeOfTransaction_49("344");
        //自定义域
        iso8583DTO.setReservedPrivate_60("01" + domain60_2);
        log.info("==================【通华线下CSB】==================【调用Channels服务】【请求参数】 iso8583DTO: {}", JSON.toJSONString(iso8583DTO));
        BaseResponse channelResponse = channelsFeign.thCSB(new ThDTO(iso8583DTO, channel));
        log.info("==================【通华线下CSB】==================【调用Channels服务】【通华-CSB接口】  channelResponse: {}", JSON.toJSONString(channelResponse));
        if (!TradeConstant.HTTP_SUCCESS.equals(channelResponse.getCode())) {
            log.info("==================【通华线下CSB】==================【调用Channels服务】【通华-CSB接口】-【请求状态码异常】");
            throw new BusinessException(EResultEnum.ORDER_CREATION_FAILED.getCode());
        }
        ISO8583DTO iso8583VO = JSON.parseObject(JSON.toJSONString(channelResponse.getData()), ISO8583DTO.class);
        log.info("==================【通华线下CSB】==================【调用Channels服务】【通华-CSB接口解析结果】  iso8583VO: {}", JSON.toJSONString(iso8583VO));
        //将46域信息按02分割
        String[] domain46 = iso8583VO.getAdditionalData_46().split("02");
        log.info("===============【通华线下CSB】===============【46域信息】 domain46: {}", Arrays.toString(domain46));
        //索引第4位 : 通华返回的商户订单号
        orders.setChannelNumber(domain46[4]);
        orders.setReportNumber(domain11 + domain60_2);
        ordersMapper.updateByPrimaryKeySelective(orders);
        //索引第5位 : 二维码URL
        String codeUrl = NumberStringUtil.hexStr2Str(domain46[5]);
        log.info("===============【通华线下CSB】===============【解析二维码URL】 codeUrl: {}", codeUrl);
        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setData(codeUrl);
        return baseResponse;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2020/5/15
     * @Descripate 通华退款
     **/
    @Override
    public BaseResponse refund(Channel channel, OrderRefund orderRefund, RabbitMassage rabbitMassage) {
        BaseResponse baseResponse = new BaseResponse();
        ThDTO thDTO = new ThDTO();
        ISO8583DTO iso8583DTO = this.creatRefundISO8583DTO(channel, orderRefund);
        thDTO.setChannel(channel);
        thDTO.setIso8583DTO(iso8583DTO);
        log.info("=================【TH退款】=================【请求Channels服务TH退款】请求参数 iso8583DTO: {} ", JSON.toJSONString(iso8583DTO));
        BaseResponse response = channelsFeign.thRefund(thDTO);
        log.info("=================【TH退款】=================【Channels服务响应】 response: {} ", JSON.toJSONString(response));

        if (response.getCode().equals(TradeConstant.HTTP_SUCCESS)) {
            JSONObject jsonObject = JSONObject.fromObject(response.getData());
            ISO8583DTO thResDTO = JSON.parseObject(String.valueOf(jsonObject), ISO8583DTO.class);
            //请求成功
            if (response.getMsg().equals("success")) {
                baseResponse.setCode(EResultEnum.SUCCESS.getCode());
                log.info("=================【TH退款】=================【退款成功】 response: {} ", JSON.toJSONString(response));
                //退款成功
                orderRefundMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_SUCCESS, thResDTO.getRetrievalReferenceNumber_37(), thResDTO.getResponseCode_39());
                //改原订单状态
                commonBusinessService.updateOrderRefundSuccess(orderRefund);
                //退还分润
                commonBusinessService.refundShareBinifit(orderRefund);
            } else {
                //退款失败
                baseResponse.setCode(EResultEnum.REFUND_FAIL.getCode());
                log.info("=================【TH退款】=================【退款失败】 response: {} ", JSON.toJSONString(response));
                baseResponse.setMsg(EResultEnum.REFUND_FAIL.getCode());
                String type = orderRefund.getRemark4().equals(TradeConstant.RF) ? TradeConstant.AA : TradeConstant.RA;
                String reconciliationRemark = type.equals(TradeConstant.AA) ? TradeConstant.REFUND_FAIL_RECONCILIATION : TradeConstant.CANCEL_ORDER_REFUND_FAIL;
                Reconciliation reconciliation = commonBusinessService.createReconciliation(type, orderRefund, reconciliationRemark);
                reconciliationMapper.insert(reconciliation);
                FundChangeDTO fundChangeDTO = new FundChangeDTO(reconciliation);
                log.info("=========================【TH退款】======================= 【调账 {}】， fundChangeDTO:【{}】", type, JSON.toJSONString(fundChangeDTO));
                BaseResponse cFundChange = clearingService.fundChange(fundChangeDTO);
                if (cFundChange.getCode().equals(TradeConstant.CLEARING_SUCCESS)) {
                    //调账成功
                    log.info("=================【TH退款】=================【调账成功】 cFundChange: {} ", JSON.toJSONString(cFundChange));
                    orderRefundMapper.updateStatuts(orderRefund.getId(), TradeConstant.REFUND_FALID, null, thResDTO.getResponseCode_39());
                    reconciliationMapper.updateStatusById(reconciliation.getId(), TradeConstant.RECONCILIATION_SUCCESS);
                    //改原订单状态
                    commonBusinessService.updateOrderRefundFail(orderRefund);
                } else {
                    //调账失败
                    log.info("=================【TH退款】=================【调账失败】 cFundChange: {} ", JSON.toJSONString(cFundChange));
                    RabbitMassage rabbitMsg = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(reconciliation));
                    log.info("=================【TH退款】=================【调账失败 上报队列 RA_AA_FAIL_DL】 rabbitMassage: {} ", JSON.toJSONString(rabbitMsg));
                    rabbitMQSender.send(AD3MQConstant.RA_AA_FAIL_DL, JSON.toJSONString(rabbitMsg));
                }
            }
        } else {
            //请求失败
            baseResponse.setCode(EResultEnum.REFUNDING.getCode());
            if (rabbitMassage == null) {
                rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
            }
            log.info("===============【TH退款】===============【请求失败 上报队列 TK_SB_FAIL_DL】 rabbitMassage: {} ", JSON.toJSONString(rabbitMassage));
            rabbitMQSender.send(AD3MQConstant.TK_SB_FAIL_DL, JSON.toJSONString(rabbitMassage));
        }
        return baseResponse;

    }


    /**
     * @return
     * @Author YangXu
     * @Date 2020/5/15
     * @Descripate 通华撤销
     **/
    @Override
    public BaseResponse cancel(Channel channel, OrderRefund orderRefund, RabbitMassage rabbitMassage) {
        RabbitMassage rabbitOrderMsg = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
        if (rabbitMassage == null) {
            rabbitMassage = rabbitOrderMsg;
        }
        BaseResponse baseResponse = new BaseResponse();
        ThDTO thDTO = new ThDTO();
        ISO8583DTO iso8583DTO = this.creatQuerryISO8583DTO(channel, orderRefund);
        thDTO.setChannel(channel);
        thDTO.setIso8583DTO(iso8583DTO);

        log.info("=================【TH撤销 cancel】=================【请求Channels服务TH退款】请求参数 iso8583DTO: {} ", JSON.toJSONString(iso8583DTO));
        BaseResponse response = channelsFeign.thQuery(thDTO);
        log.info("=================【TH撤销 cancel】=================【Channels服务响应】 response: {} ", JSON.toJSONString(response));
        if (response.getCode().equals(TradeConstant.HTTP_SUCCESS)) {
            //请求成功
            //请求成功
            JSONObject jsonObject = JSONObject.fromObject(response.getData());
            ISO8583DTO iso8583DTO1 = JSON.parseObject(String.valueOf(jsonObject), ISO8583DTO.class);
            if (iso8583DTO1.getAdditionalData_46().equals("1")) {
                //交易成功
                //更新订单状态
                if (ordersMapper.updateOrderByAd3Query(orderRefund.getOrderId(), TradeConstant.ORDER_PAY_SUCCESS, iso8583DTO1.getRetrievalReferenceNumber_37(), new Date()) == 1) {
                    //更新成功
                    baseResponse = this.cancelPaying(channel, orderRefund, null);
                } else {
                    baseResponse.setCode(EResultEnum.REFUNDING.getCode());
                    //更新失败后去查询订单信息
                    rabbitMQSender.send(AD3MQConstant.E_CX_GX_FAIL_DL, JSON.toJSONString(rabbitMassage));
                }
            } else if (iso8583DTO1.getAdditionalData_46().equals("3")) {
                //交易中
                baseResponse.setCode(EResultEnum.REFUNDING.getCode());
                log.info("=================【TH撤销 cancel】=================【查询订单失败】orderId : {}", orderRefund.getOrderId());
                rabbitMQSender.send(AD3MQConstant.E_CX_GX_FAIL_DL, JSON.toJSONString(rabbitMassage));
            } else {
                //交易失败
                baseResponse.setCode(EResultEnum.REFUND_FAIL.getCode());
                log.info("=================【TH撤销 cancel】================= 【交易失败】orderId : {}", orderRefund.getOrderId());
                ordersMapper.updateOrderByAd3Query(orderRefund.getOrderId(), TradeConstant.ORDER_PAY_FAILD, iso8583DTO1.getRetrievalReferenceNumber_37(), new Date());
            }

        } else {
            //请求失败
            baseResponse.setCode(EResultEnum.REFUNDING.getCode());
            log.info("=================【TH撤销 cancel】=================【查询订单失败】orderId : {}", orderRefund.getOrderId());
            rabbitMQSender.send(AD3MQConstant.E_CX_GX_FAIL_DL, JSON.toJSONString(rabbitMassage));
        }
        return baseResponse;
    }


    @Override
    public BaseResponse cancelPaying(Channel channel, OrderRefund orderRefund, RabbitMassage rabbitMassage) {
        BaseResponse baseResponse = new BaseResponse();
        Orders orders = ordersMapper.selectByPrimaryKey(orderRefund.getOrderId());
        ThDTO thDTO = new ThDTO();
        ISO8583DTO iso8583DTO = this.creatRefundISO8583DTO(channel, orderRefund);
        thDTO.setChannel(channel);
        thDTO.setIso8583DTO(iso8583DTO);
        log.info("=================【TH撤销 cancelPaying】=================【请求Channels服务TH退款】请求参数 iso8583DTO: {} ", JSON.toJSONString(iso8583DTO));
        BaseResponse response = channelsFeign.thRefund(thDTO);
        log.info("=================【TH撤销 cancelPaying】=================【Channels服务响应】 response: {} ", JSON.toJSONString(response));

        if (response.getCode().equals(TradeConstant.HTTP_SUCCESS)) {
            JSONObject jsonObject = JSONObject.fromObject(response.getData());
            ISO8583DTO thResDTO = JSON.parseObject(String.valueOf(jsonObject), ISO8583DTO.class);
            //请求成功
            if (response.getMsg().equals("success")) {
                //退款成功
                baseResponse.setCode(EResultEnum.SUCCESS.getCode());
                log.info("=================【TH撤销 cancelPaying】=================【撤销成功】orderId : {}", orders.getId());
                ordersMapper.updateOrderCancelStatus(orders.getMerchantOrderId(), orderRefund.getOperatorId(), TradeConstant.ORDER_CANNEL_SUCCESS);
            } else {
                //退款失败
                baseResponse.setCode(EResultEnum.REFUND_FAIL.getCode());
                log.info("=================【TH撤销 cancelPaying】=================【撤销失败】orderId : {}", orders.getId());
                ordersMapper.updateOrderCancelStatus(orders.getMerchantOrderId(), orderRefund.getOperatorId(), TradeConstant.ORDER_CANNEL_FALID);
            }
        } else {
            //请求失败
            baseResponse.setCode(EResultEnum.REFUNDING.getCode());
            log.info("=================【TH撤销 cancelPaying】=================【请求失败】orderId : {}", orders.getId());
            if (rabbitMassage == null) {
                rabbitMassage = new RabbitMassage(AsianWalletConstant.THREE, JSON.toJSONString(orderRefund));
            }
            log.info("=================【TH撤销 cancelPaying】=================【上报通道】rabbitMassage: {} ", JSON.toJSON(rabbitMassage));
            rabbitMQSender.send(AD3MQConstant.CX_SB_FAIL_DL, JSON.toJSONString(rabbitMassage));
        }
        return baseResponse;
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2020/5/18
     * @Descripate 创建退款DTO
     **/
    private ISO8583DTO creatRefundISO8583DTO(Channel channel, OrderRefund orderRefund) {
        ISO8583DTO iso8583DTO = new ISO8583DTO();
        iso8583DTO.setMessageType("0200");
        iso8583DTO.setProcessingCode_3("400100");
        iso8583DTO.setAmountOfTransactions_4(NumberStringUtil.addLeftChar(orderRefund.getTradeAmount().toString().replace(".", ""), 12, '0'));
        iso8583DTO.setSystemTraceAuditNumber_11(orderRefund.getOrderId().substring(0, 6));
        iso8583DTO.setPointOfServiceEntryMode_22("030");
        iso8583DTO.setPointOfServiceConditionMode_25("00");
        //iso8583DTO.setAcquiringInstitutionIdentificationCode_32(); 机构号
        //iso8583DTO.setCardAcceptorTerminalIdentification_41();      //卡机终端标识码
        //iso8583DTO.setCardAcceptorIdentificationCode_42();          //受卡方标识码

        String s46 = "303002020202" + orderRefund.getChannelNumber() + "0202";
        BerTlvBuilder berTlvBuilder = new BerTlvBuilder();
        //这里的Tag要用16进制,Length是自动算出来的,最后是要存的数据
        berTlvBuilder.addHex(new BerTag(0x5F52),s46);
        byte[] bytes = berTlvBuilder.buildArray();
        ////转成Hex码来传输
        String hexString = HexUtil.toHexString(bytes);
        iso8583DTO.setAdditionalData_46("5F" + hexString);

        iso8583DTO.setCurrencyCodeOfTransaction_49("344");
        iso8583DTO.setReservedPrivate_60("55"+"");//批次号


        return iso8583DTO;
    }
    /**
     * @return
     * @Author YangXu
     * @Date 2020/5/18
     * @Descripate 创建查询DTO
     **/
    private ISO8583DTO creatQuerryISO8583DTO(Channel channel, OrderRefund orderRefund) {
        ISO8583DTO iso8583DTO = new ISO8583DTO();
        String timeStamp = System.currentTimeMillis() + "";
        iso8583DTO.setMessageType("0200");
        iso8583DTO.setProcessingCode_3("700206");
        iso8583DTO.setAmountOfTransactions_4(NumberStringUtil.addLeftChar(orderRefund.getTradeAmount().toString().replace(".", ""), 12, '0'));
        //受卡方系统跟踪号
        iso8583DTO.setSystemTraceAuditNumber_11(timeStamp.substring(0, 6));
        //服务点输入方式码
        iso8583DTO.setPointOfServiceEntryMode_22("030");
        //服务点条件码
        iso8583DTO.setPointOfServiceConditionMode_25("00");
        //受理方标识码 (机构号)
        //iso8583DTO.setAcquiringInstitutionIdentificationCode_32("08600005");
        ////受卡机终端标识码 (设备号)
        //iso8583DTO.setCardAcceptorTerminalIdentification_41("00018644");
        ////受卡方标识码 (商户号)
        //iso8583DTO.setCardAcceptorIdentificationCode_42("852999958120501");

        //附加信息
        String s46 = "303002020202" + orderRefund.getChannelNumber() + "0202";
        BerTlvBuilder berTlvBuilder = new BerTlvBuilder();
        //这里的Tag要用16进制,Length是自动算出来的,最后是要存的数据
        berTlvBuilder.addHex(new BerTag(0x5F52),s46);
        byte[] bytes = berTlvBuilder.buildArray();
        ////转成Hex码来传输
        String hexString = HexUtil.toHexString(bytes);
        iso8583DTO.setAdditionalData_46("5F" + hexString);

        //交易货币代码
        iso8583DTO.setCurrencyCodeOfTransaction_49("344");
        //自定义域
        iso8583DTO.setReservedPrivate_60("01" + timeStamp.substring(6, 12));


        return iso8583DTO;
    }
}
