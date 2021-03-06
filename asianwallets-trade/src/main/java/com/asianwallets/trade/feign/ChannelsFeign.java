package com.asianwallets.trade.feign;

import com.asianwallets.common.dto.ad3.AD3BSCScanPayDTO;
import com.asianwallets.common.dto.ad3.AD3CSBScanPayDTO;
import com.asianwallets.common.dto.ad3.AD3ONOFFRefundDTO;
import com.asianwallets.common.dto.ad3.AD3OnlineAcquireDTO;
import com.asianwallets.common.dto.alipay.*;
import com.asianwallets.common.dto.doku.DOKUReqDTO;
import com.asianwallets.common.dto.eghl.EGHLRequestDTO;
import com.asianwallets.common.dto.enets.EnetsBankRequestDTO;
import com.asianwallets.common.dto.enets.EnetsOffLineRequestDTO;
import com.asianwallets.common.dto.help2pay.Help2PayOutDTO;
import com.asianwallets.common.dto.help2pay.Help2PayRequestDTO;
import com.asianwallets.common.dto.megapay.*;
import com.asianwallets.common.dto.nganluong.NganLuongDTO;
import com.asianwallets.common.dto.qfpay.QfPayDTO;
import com.asianwallets.common.dto.th.ISO8583.ThDTO;
import com.asianwallets.common.dto.upi.UpiDTO;
import com.asianwallets.common.dto.vtc.VTCRequestDTO;
import com.asianwallets.common.dto.wechat.*;
import com.asianwallets.common.dto.xendit.XenditDTO;
import com.asianwallets.common.response.BaseResponse;
import com.asianwallets.trade.feign.impl.ChannelsFeignImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@FeignClient(value = "asianwallets-channels", fallback = ChannelsFeignImpl.class)
public interface ChannelsFeign {

    @ApiOperation(value = "支付宝线上下单接口")
    @PostMapping("aliPayWebsite")
    BaseResponse aliPayWebsite(@RequestBody @ApiParam @Valid AliPayWebDTO aliPayWebDTO);

    @ApiOperation(value = "支付宝线下BSC接口")
    @PostMapping("/aliPay/aliPayOfflineBSC")
    BaseResponse aliPayOfflineBSC(@RequestBody @ApiParam @Valid AliPayOfflineBSCDTO aliPayOfflineBSCDTO);

    @ApiOperation(value = "支付宝CSB接口")
    @PostMapping("/aliPay/aliPayCSB")
    BaseResponse aliPayCSB(@RequestBody @ApiParam @Valid AliPayCSBDTO aliPayCSBDTO);

    @ApiOperation(value = "支付宝退款接口")
    @PostMapping("/aliPay/alipayRefund")
    BaseResponse alipayRefund(@RequestBody @ApiParam @Valid AliPayRefundDTO aliPayRefundDTO);

    @ApiOperation(value = "支付宝查询接口")
    @PostMapping("/aliPay/alipayQuery")
    BaseResponse alipayQuery(@RequestBody @ApiParam @Valid AliPayQueryDTO aliPayQueryDTO);

    @ApiOperation(value = "支付宝撤销接口")
    @PostMapping("/aliPay/alipayCancel")
    BaseResponse alipayCancel(@RequestBody @ApiParam @Valid AliPayCancelDTO aliPayCancelDTO);

    @ApiOperation("微信查询接口")
    @PostMapping("/wechat/wechatQuery")
    BaseResponse wechatQuery(@RequestBody @ApiParam @Valid WechatQueryDTO wechatQueryDTO);

    @ApiOperation("微信线下BSC接口")
    @PostMapping("/wechat/wechatBSC")
    BaseResponse wechatOfflineBSC(@RequestBody @ApiParam @Valid WechatBSCDTO wechatBSCDTO);

    @ApiOperation("微信线下CSB接口")
    @PostMapping("/wechat/wechatCSB")
    BaseResponse wechatOfflineCSB(@RequestBody @ApiParam @Valid WechatCSBDTO wechatCSBDTO);

    @ApiOperation("微信退款接口")
    @PostMapping("/wechat/wechatRefund")
    BaseResponse wechatRefund(@RequestBody @ApiParam @Valid WechaRefundDTO wechaRefundDTO);

    @ApiOperation(value = "微信撤销接口")
    @PostMapping("/wechat/wechatCancel")
    BaseResponse wechatCancel(@RequestBody @ApiParam @Valid WechatCancelDTO wechatCancelDTO);

    @ApiOperation(value = "eghl收单接口")
    @PostMapping("/eghl/eGHLPay")
    BaseResponse eGHLPay(@RequestBody @ApiParam @Valid EGHLRequestDTO eghlRequestDTO);

    @ApiOperation(value = "megaPayTHB网银收单接口")
    @PostMapping("/megaPay/megaPayTHB")
    BaseResponse megaPayTHB(@RequestBody @ApiParam @Valid MegaPayRequestDTO megaPayRequestDTO);

    @ApiOperation(value = "megaPay查询接口")
    @PostMapping("/megaPay/megaPayQuery")
    BaseResponse megaPayQuery(@RequestBody @ApiParam @Valid MegaPayQueryDTO megaPayQueryDTO);

    @ApiOperation(value = "megaPayIDR网银收单接口")
    @PostMapping("/megaPay/megaPayIDR")
    BaseResponse megaPayIDR(@RequestBody @ApiParam @Valid MegaPayIDRRequestDTO megaPayIDRRequestDTO);

    @ApiOperation(value = "nextPos-Csb接口")
    @PostMapping("/megaPay/nextPosCsb")
    BaseResponse nextPosCsb(@RequestBody @ApiParam @Valid NextPosRequestDTO nextPosRequestDTO);

    @ApiOperation(value = "nextPos查询接口")
    @PostMapping("/megaPay/nextPosQuery")
    BaseResponse nextPosQuery(@RequestBody @ApiParam NextPosQueryDTO nextPosQueryDTO);

    @ApiOperation(value = "nextPos退款接口")
    @PostMapping("/megaPay/nextPosRefund")
    BaseResponse nextPosRefund(@RequestBody @ApiParam NextPosRefundDTO nextPosRefundDTO);

    @ApiOperation(value = "vtc收单接口")
    @PostMapping("/vtc/vtcPay")
    BaseResponse vtcPay(@RequestBody @ApiParam @Valid VTCRequestDTO vtcRequestDTO);

    @ApiOperation(value = "enets网银收单接口")
    @PostMapping("enets/eNetsDebitPay")
    BaseResponse eNetsBankPay(@RequestBody @ApiParam @Valid EnetsBankRequestDTO enetsBankRequestDTO);

    @ApiOperation(value = "enetsPos线下CSB")
    @PostMapping("enets/eNetsPosCSBPay")
    BaseResponse eNetsPosCSBPay(@RequestBody @ApiParam @Valid EnetsOffLineRequestDTO enetsOffLineRequestDTO);

    @ApiOperation(value = "help2Pay网银收单接口")
    @PostMapping("help/help2pay")
    BaseResponse help2Pay(@RequestBody @ApiParam @Valid Help2PayRequestDTO help2PayRequestDTO);

    @ApiOperation(value = "help2Pay网银汇款接口")
    @PostMapping("help/help2PayOut")
    BaseResponse help2PayOut(@RequestBody @ApiParam @Valid Help2PayOutDTO help2PayOutDTO);

    @ApiOperation(value = "nganLuong网银收单接口")
    @PostMapping("nganLuong/nganLuongPay")
    BaseResponse nganLuongPay(@RequestBody @ApiParam @Valid NganLuongDTO nganLuongDTO);

    @ApiOperation(value = "xendit网银收单接口")
    @PostMapping("xendit/xenditPay")
    BaseResponse xenditPay(@RequestBody @ApiParam @Valid XenditDTO xenditDTO);

    @ApiOperation(value = "Doku收单接口")
    @PostMapping("doku/payment")
    BaseResponse dokuPay(DOKUReqDTO dokuReqDTO);

    @ApiOperation(value = "Doku查询接口")
    @PostMapping("doku/checkStatus")
    BaseResponse checkStatus(DOKUReqDTO dokuReqDTO);

    @ApiOperation(value = "Doku退款接口")
    @PostMapping("doku/refund")
    BaseResponse dokuRefund(DOKUReqDTO dokuReqDTO);

    @ApiOperation(value = "AD3线下CSB")
    @PostMapping("/ad3/offlineCsb")
    BaseResponse ad3OfflineCsb(@RequestBody @ApiParam AD3CSBScanPayDTO ad3CSBScanPayDTO);

    @ApiOperation(value = "AD3线下BSC")
    @PostMapping("/ad3/offlineBsc")
    BaseResponse ad3OfflineBsc(@RequestBody @ApiParam AD3BSCScanPayDTO ad3CSBScanPayDTO);

    @ApiOperation(value = "AD3线上")
    @PostMapping("/ad3/onlinePay")
    BaseResponse ad3OnlinePay(@RequestBody @ApiParam AD3OnlineAcquireDTO ad3OnlineAcquireDTO);

    @ApiOperation(value = "AD3线下退款接口")
    @PostMapping("/ad3/offlineRefund")
    BaseResponse ad3OfflineRefund(@RequestBody @ApiParam AD3ONOFFRefundDTO ad3RefundDTO);

    @ApiOperation(value = "AD3线上退款接口")
    @PostMapping("/ad3/onlineRefund")
    BaseResponse ad3OnlineRefund(@RequestBody @ApiParam AD3ONOFFRefundDTO sendAdRefundDTO);

    @ApiOperation(value = "AD3查询接口")
    @PostMapping("/ad3/query")
    BaseResponse query(@RequestBody @ApiParam AD3ONOFFRefundDTO ad3ONOFFRefundDTO);

    @ApiOperation(value = "QfPayCSB收单接口")
    @PostMapping("qfPay/qfPayCSB")
    BaseResponse qfPayCSB(QfPayDTO qfPayDTO);

    @ApiOperation(value = "QfPayBSC收单接口")
    @PostMapping("qfPay/qfPayBSC")
    BaseResponse qfPayBSC(QfPayDTO qfPayDTO);

    @ApiOperation(value = "QfPay查询接口")
    @PostMapping("qfPay/qfPayQuery")
    BaseResponse qfPayQuery(QfPayDTO qfPayDTO);

    @ApiOperation(value = "QfPay退款接口")
    @PostMapping("qfPay/qfPayRefund")
    BaseResponse qfPayRefund(QfPayDTO qfPayDTO);

    @ApiOperation(value = "QfPay退款查询接口")
    @PostMapping("qfPay/qfPayRefundSearch")
    BaseResponse qfPayRefundSearch(QfPayDTO qfPayDTO);


    @ApiOperation(value = "通华CSB收单接口")
    @PostMapping("/th/thCSB")
    BaseResponse thCSB(ThDTO thDTO);

    @ApiOperation(value = "通华BSC收单接口")
    @PostMapping("/th/thBSC")
    BaseResponse thBSC(ThDTO thDTO);

    @ApiOperation(value = "通华银行卡收单接口")
    @PostMapping("/th/thBankCard")
    BaseResponse thBankCard(ThDTO thDTO);

    @ApiOperation(value = "通华退款接口")
    @PostMapping("/th/thRefund")
    BaseResponse thRefund(ThDTO thDTO);

    @ApiOperation(value = "通华查询接口")
    @PostMapping("/th/thQuery")
    BaseResponse thQuery(ThDTO thDTO);

    @ApiOperation(value = "通华冲正接口")
    @PostMapping("/th/thBankCardReverse")
    BaseResponse thBankCardReverse(ThDTO thDTO);

    @ApiOperation(value = "通华签到接口")
    @PostMapping("/th/thSign")
    BaseResponse thSign(ThDTO thDTO);

    @ApiOperation("通华预授权")
    @PostMapping("/th/preAuth")
    BaseResponse preAuth(@RequestBody @ApiParam ThDTO thDTO);

    @ApiOperation("通华预授权冲正")
    @PostMapping("/th/preAuthReverse")
    BaseResponse preAuthReverse(@RequestBody @ApiParam ThDTO thDTO);

    @ApiOperation("通华预授权撤销")
    @PostMapping("/th/preAuthRevoke")
    BaseResponse preAuthRevoke(@RequestBody @ApiParam ThDTO thDTO);

    @ApiOperation("通华预授权完成")
    @PostMapping("/th/preAuthComplete")
    BaseResponse preAuthComplete(@RequestBody @ApiParam ThDTO thDTO);

    @ApiOperation("通华预授权完成撤销")
    @PostMapping("/th/preAuthCompleteRevoke")
    BaseResponse preAuthCompleteRevoke(@RequestBody @ApiParam ThDTO thDTO);

    @ApiOperation(value = "upi付款接口")
    @PostMapping("/upi/upiPay")
    BaseResponse upiPay(UpiDTO upiDTO);

    @ApiOperation("upi银行接口")
    @PostMapping("/upi/upiBankPay")
    BaseResponse upiBankPay(UpiDTO upiDTO);

    @ApiOperation(value = "upi退款接口")
    @PostMapping("/upi/upiRefund")
    BaseResponse upiRefund(UpiDTO upiDTO);

    @ApiOperation(value = "upi撤销接口")
    @PostMapping("/upi/upiCancel")
    BaseResponse upiCancel(UpiDTO upiDTO);

    @ApiOperation(value = "upi查询接口")
    @PostMapping("/upi/upiQuery")
    BaseResponse upiQueery(UpiDTO upiDTO);

    @ApiOperation(value = "通华银行卡退款接口")
    @PostMapping("/th/thBankCardRefund")
    BaseResponse thBankCardRefund(ThDTO thDTO);

    @ApiOperation(value = "通华银行卡撤销接口")
    @PostMapping("/th/thBankCardUndo")
    BaseResponse thBankCardUndo(ThDTO thDTO);
}
