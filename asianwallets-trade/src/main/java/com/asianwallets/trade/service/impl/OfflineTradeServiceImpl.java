package com.asianwallets.trade.service.impl;

import com.alibaba.fastjson.JSON;
import com.asianwallets.common.constant.TradeConstant;
import com.asianwallets.common.entity.*;
import com.asianwallets.common.exception.BusinessException;
import com.asianwallets.common.redis.RedisService;
import com.asianwallets.common.response.EResultEnum;
import com.asianwallets.common.utils.DateToolUtils;
import com.asianwallets.common.vo.SysUserVO;
import com.asianwallets.trade.dao.DeviceBindingMapper;
import com.asianwallets.trade.dao.OrdersMapper;
import com.asianwallets.trade.dao.SysUserMapper;
import com.asianwallets.trade.dto.OfflineTradeDTO;
import com.asianwallets.trade.service.CommonBusinessService;
import com.asianwallets.trade.service.CommonRedisDataService;
import com.asianwallets.trade.service.OfflineTradeService;
import com.asianwallets.trade.vo.CsbDynamicScanVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

@Service
@Slf4j
public class OfflineTradeServiceImpl implements OfflineTradeService {

    @Autowired
    private CommonBusinessService commonBusinessService;

    @Autowired
    private CommonRedisDataService commonRedisDataService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private DeviceBindingMapper deviceBindingMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 校验请求参数
     *
     * @param offlineTradeDTO              线下交易输入实体
     * @param institutionRequestParameters 机构请求参数实体
     */
    private void checkRequestParameters(OfflineTradeDTO offlineTradeDTO, InstitutionRequestParameters institutionRequestParameters) {
        if (institutionRequestParameters.getBrowserUrl() && StringUtils.isEmpty(offlineTradeDTO.getBrowserUrl())) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (institutionRequestParameters.getIssuerId() && StringUtils.isEmpty(offlineTradeDTO.getIssuerId())) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (institutionRequestParameters.getProductName() && StringUtils.isEmpty(offlineTradeDTO.getProductName())) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (institutionRequestParameters.getProductDescription() && StringUtils.isEmpty(offlineTradeDTO.getProductDescription())) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (institutionRequestParameters.getPayerName() && StringUtils.isEmpty(offlineTradeDTO.getPayerName())) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (institutionRequestParameters.getPayerPhone() && StringUtils.isEmpty(offlineTradeDTO.getPayerPhone())) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (institutionRequestParameters.getPayerEmail() && StringUtils.isEmpty(offlineTradeDTO.getPayerEmail())) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (institutionRequestParameters.getPayerBank() && StringUtils.isEmpty(offlineTradeDTO.getPayerBank())) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (institutionRequestParameters.getLanguage() && StringUtils.isEmpty(offlineTradeDTO.getLanguage())) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (institutionRequestParameters.getRemark1() && StringUtils.isEmpty(offlineTradeDTO.getRemark1())) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (institutionRequestParameters.getRemark2() && StringUtils.isEmpty(offlineTradeDTO.getRemark2())) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        if (institutionRequestParameters.getRemark3() && StringUtils.isEmpty(offlineTradeDTO.getRemark3())) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
    }

    /**
     * 校验输入参数的合法性
     *
     * @param offlineTradeDTO 线下交易输入实体
     */
    private void checkParamValidity(OfflineTradeDTO offlineTradeDTO) {
        //验签
        if (!commonBusinessService.checkSignByMd5(offlineTradeDTO)) {
            log.info("==================【线下CSB动态扫码】==================【签名不匹配】");
            throw new BusinessException(EResultEnum.DECRYPTION_ERROR.getCode());
        }
        //校验订单金额
        if (offlineTradeDTO.getOrderAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.info("==================【线下CSB动态扫码】==================【订单金额不合法】");
            throw new BusinessException(EResultEnum.REFUND_AMOUNT_NOT_LEGAL.getCode());
        }
        //校验Token信息
        SysUserVO sysUserVO = JSON.parseObject(redisService.get(offlineTradeDTO.getToken()), SysUserVO.class);
        if (sysUserVO == null || !(offlineTradeDTO.getOperatorId().concat(offlineTradeDTO.getMerchantId()).equals(sysUserVO.getUsername()))) {
            log.info("==================【线下CSB动态扫码】==================【Token不合法】");
            throw new BusinessException(EResultEnum.TOKEN_IS_INVALID.getCode());
        }
        //校验币种信息
        if (!commonBusinessService.checkOrderCurrency(offlineTradeDTO.getOrderCurrency(), offlineTradeDTO.getOrderAmount())) {
            log.info("==================【线下CSB动态扫码】==================【订单金额不符合币种默认值】");
            throw new BusinessException(EResultEnum.REFUND_AMOUNT_NOT_LEGAL.getCode());
        }
        //校验订单号
        if (ordersMapper.selectByMerchantOrderId(offlineTradeDTO.getOrderNo()) != null) {
            log.info("==================【线下CSB动态扫码】==================【商户订单号已存在】");
            throw new BusinessException(EResultEnum.INSTITUTION_ORDER_ID_EXIST.getCode());
        }
        //校验设备信息
        checkDevice(offlineTradeDTO.getMerchantId(), offlineTradeDTO.getImei(), offlineTradeDTO.getOperatorId());
    }

    /**
     * 校验设备信息
     *
     * @param merchantId 商户ID
     * @param imei       设备号
     * @param operatorId 操作员ID
     */
    private void checkDevice(String merchantId, String imei, String operatorId) {
        //校验商户绑定设备
        DeviceBinding deviceBinding = deviceBindingMapper.selectByMerchantIdAndImei(merchantId, imei);
        if (deviceBinding == null) {
            log.info("================【线下业务接口】================【设备编号不合法】");
            throw new BusinessException(EResultEnum.DEVICE_CODE_INVALID.getCode());
        }
        //校验设备操作员
        SysUser sysUser = sysUserMapper.selectByUsername(operatorId.concat(merchantId));
        if (sysUser == null) {
            log.info("================【线下业务接口】================【设备操作员不合法】");
            throw new BusinessException(EResultEnum.DEVICE_OPERATOR_INVALID.getCode());
        }
    }

    /**
     * 设置订单属性
     *
     * @param offlineTradeDTO 线下交易输入实体
     * @return 订单
     */
    private Orders setAttributes(OfflineTradeDTO offlineTradeDTO) {
        Orders orders = new Orders();
        orders.setMerchantId(offlineTradeDTO.getMerchantId());
        orders.setMerchantOrderId(offlineTradeDTO.getOrderNo());
        orders.setOrderCurrency(offlineTradeDTO.getOrderCurrency());
        orders.setOrderAmount(offlineTradeDTO.getOrderAmount());
        orders.setMerchantOrderTime(DateToolUtils.getReqDateG(offlineTradeDTO.getOrderTime()));
        orders.setProductCode(offlineTradeDTO.getProductCode());
        orders.setImei(offlineTradeDTO.getImei());
        orders.setOperatorId(offlineTradeDTO.getOperatorId());
        orders.setCreateTime(new Date());
        orders.setCreator(offlineTradeDTO.getMerchantId());
        return orders;
    }

    /**
     * 线下同机构CSB动态扫码
     *
     * @param offlineTradeDTO 线下交易输入实体
     * @return 线下同机构CSB动态扫码输出实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class, noRollbackFor = BusinessException.class)
    public CsbDynamicScanVO csbDynamicScan(OfflineTradeDTO offlineTradeDTO) {
        log.info("==================【线下CSB动态扫码】==================【请求参数】 offlineTradeDTO: {}", JSON.toJSONString(offlineTradeDTO));
        //重复请求
        if (!commonBusinessService.repeatedRequests(offlineTradeDTO.getMerchantId(), offlineTradeDTO.getOrderNo())) {
            log.info("==================【线下CSB动态扫码】==================【重复请求】");
            throw new BusinessException(EResultEnum.REPEAT_ORDER_REQUEST.getCode());
        }
        Merchant merchant = commonRedisDataService.getMerchantById(offlineTradeDTO.getMerchantId());
        Institution institution = commonRedisDataService.getInstitutionById(merchant.getInstitutionId());
        InstitutionRequestParameters institutionRequestParameters = commonRedisDataService.getInstitutionRequestByIdAndDirection(institution.getId(), TradeConstant.TRADE_UPLINE);
        //校验机构必填请求输入参数
        checkRequestParameters(offlineTradeDTO, institutionRequestParameters);
        //校验输入参数合法性
        checkParamValidity(offlineTradeDTO);
        //设置订单属性
        Orders orders = setAttributes(offlineTradeDTO);
        CsbDynamicScanVO csbDynamicScanVO = new CsbDynamicScanVO();
        csbDynamicScanVO.setOrderNo(orders.getMerchantOrderId());
        csbDynamicScanVO.setQrCodeUrl("www.baidu.com");
        csbDynamicScanVO.setDecodeType("0");
        log.info("==================【线下CSB动态扫码】==================【下单结束】");
        return csbDynamicScanVO;
    }
}
