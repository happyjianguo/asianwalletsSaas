package com.asianwallets.base.service.impl;

import com.asianwallets.base.dao.OrderRefundMapper;
import com.asianwallets.base.dao.OrdersMapper;
import com.asianwallets.base.dao.TradeCheckAccountDetailMapper;
import com.asianwallets.base.dao.TradeCheckAccountMapper;
import com.asianwallets.base.feign.MessageFeign;
import com.asianwallets.base.service.TradeCheckAccountService;
import com.asianwallets.base.vo.CheckAccountListVO;
import com.asianwallets.base.vo.CheckAccountVO;
import com.asianwallets.common.config.AuditorProvider;
import com.asianwallets.common.constant.AsianWalletConstant;
import com.asianwallets.common.constant.TradeConstant;
import com.asianwallets.common.dto.TradeCheckAccountDTO;
import com.asianwallets.common.entity.OrderRefund;
import com.asianwallets.common.entity.Orders;
import com.asianwallets.common.entity.TradeCheckAccount;
import com.asianwallets.common.entity.TradeCheckAccountDetail;
import com.asianwallets.common.exception.BusinessException;
import com.asianwallets.common.response.EResultEnum;
import com.asianwallets.common.utils.ArrayUtil;
import com.asianwallets.common.utils.DateToolUtils;
import com.asianwallets.common.utils.IDS;
import com.asianwallets.common.vo.ExportTradeAccountVO;
import com.asianwallets.common.vo.TradeAccountDetailVO;
import com.asianwallets.common.vo.TradeCheckAccountDetailVO;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class TradeCheckAccountServiceImpl implements TradeCheckAccountService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderRefundMapper orderRefundMapper;

    @Autowired
    private TradeCheckAccountMapper tradeCheckAccountMapper;

    @Autowired
    private TradeCheckAccountDetailMapper tradeCheckAccountDetailMapper;

    @Autowired
    private MessageFeign messageFeign;

    @Autowired
    private AuditorProvider auditorProvider;

    @Value("custom.warning.mobile")
    private String warningMobile;

    @Value("custom.warning.email")
    private String warningEmail;


    /**
     * 生成昨日商户交易对账单
     */
    @Override
    @Transactional
    public void tradeCheckAccount() {
        try {
            //昨日日期
            String yesterday = DateToolUtils.getYesterday();
            //交易订单与退款订单汇总信息计算
            List<CheckAccountVO> checkAccountVOList = ordersMapper.tradeAccountCheck(yesterday);
            if (ArrayUtil.isEmpty(checkAccountVOList)) {
                log.info("==================【商户交易对账单】==================【昨日订单信息数据为空】");
                return;
            }
            List<TradeCheckAccount> tradeCheckAccounts = new ArrayList<>();
            for (CheckAccountVO checkAccountVO : checkAccountVOList) {
                //商户对账单总表
                TradeCheckAccount tradeCheckAccount = new TradeCheckAccount();
                tradeCheckAccount.setId(IDS.uuid2());
                tradeCheckAccount.setMerchantId(checkAccountVO.getMerchantId());
                tradeCheckAccount.setMerchantName(checkAccountVO.getMerchantName());
                tradeCheckAccount.setInstitutionId(checkAccountVO.getInstitutionId());
                tradeCheckAccount.setInstitutionName(checkAccountVO.getInstitutionName());
                tradeCheckAccount.setCurrency(checkAccountVO.getOrderCurrency());
                tradeCheckAccount.setTradeTime(DateToolUtils.getDateByStr(yesterday));
                //手续费: 收单手续费 + 退款手续费
                BigDecimal fee = BigDecimal.ZERO;
                for (CheckAccountListVO checkAccountListVO : checkAccountVO.getCheckAccountListVOList()) {
                    if ("1".equals(checkAccountListVO.getType())) {
                        //收单
                        fee = fee.add(checkAccountListVO.getFee());
                        //收单总金额
                        tradeCheckAccount.setTotalTradeAmount(checkAccountListVO.getTotalAmount());
                        //收单总笔数
                        tradeCheckAccount.setTotalTradeCount(checkAccountListVO.getTotalCount());
                    } else {
                        //退款单
                        fee = fee.add(checkAccountListVO.getRefundFee());
                        //退款总金额
                        tradeCheckAccount.setTotalRefundAmount(checkAccountListVO.getRefundAmount());
                        //退款总笔数
                        tradeCheckAccount.setTotalRefundCount(checkAccountListVO.getRefundCount());
                    }
                }
                tradeCheckAccount.setFee(fee);
                tradeCheckAccount.setCreateTime(new Date());
                tradeCheckAccounts.add(tradeCheckAccount);
            }
            //交易订单
            List<Orders> ordersList = ordersMapper.selectByDate(yesterday);
            //退款订单
            List<OrderRefund> orderRefundList = orderRefundMapper.selectByDate(yesterday);
            List<TradeCheckAccountDetail> tradeCheckAccountDetails = new ArrayList<>();
            for (Orders order : ordersList) {
                TradeCheckAccountDetail tradeCheckAccountDetail = new TradeCheckAccountDetail();
                BeanUtils.copyProperties(order, tradeCheckAccountDetail);
                tradeCheckAccountDetail.setId(IDS.uuid2());
                //订单id
                tradeCheckAccountDetail.setOrderId(order.getId());
                //订单创建时间
                tradeCheckAccountDetail.setOrderCreateTime(order.getCreateTime());
                //支付方式
                tradeCheckAccountDetail.setPayType(order.getPayMethod());
                //创建时间
                tradeCheckAccountDetail.setCreateTime(new Date());
                //收单
                tradeCheckAccountDetail.setTradeType(AsianWalletConstant.PAYMENT);
                tradeCheckAccountDetails.add(tradeCheckAccountDetail);
            }
            for (OrderRefund orderRefund : orderRefundList) {
                TradeCheckAccountDetail tradeCheckAccountDetail = new TradeCheckAccountDetail();
                BeanUtils.copyProperties(orderRefund, tradeCheckAccountDetail);
                tradeCheckAccountDetail.setId(IDS.uuid2());
                //退款订单id
                tradeCheckAccountDetail.setOrderId(orderRefund.getId());
                //订单创建时间
                tradeCheckAccountDetail.setOrderCreateTime(orderRefund.getCreateTime());
                //支付方式
                tradeCheckAccountDetail.setPayType(orderRefund.getPayMethod());
                //退款状态
                tradeCheckAccountDetail.setRefundStatus(orderRefund.getRefundStatus());
                //交易完成时间
                tradeCheckAccountDetail.setChannelCallbackTime(orderRefund.getUpdateTime());
                //退款费率
                tradeCheckAccountDetail.setRate(orderRefund.getRefundRate());
                //退款费率类型
                tradeCheckAccountDetail.setRateType(orderRefund.getRefundRateType());
                //退款手续费
                tradeCheckAccountDetail.setFee(orderRefund.getRefundFee());
                //退款
                tradeCheckAccountDetail.setTradeType(AsianWalletConstant.REFUND);
                //创建时间
                tradeCheckAccountDetail.setCreateTime(new Date());
                tradeCheckAccountDetails.add(tradeCheckAccountDetail);
            }
            tradeCheckAccountMapper.insertList(tradeCheckAccounts);
            tradeCheckAccountDetailMapper.insertList(tradeCheckAccountDetails);
        } catch (Exception e) {
            log.info("==================【商户交易对账单】==================【定时任务异常】", e);
            messageFeign.sendSimple(warningMobile, "SAAS-昨日的商户交易对账定时任务出错!");
            messageFeign.sendSimpleMail(warningEmail, "SAAS-昨日的商户交易对账定时任务出错!", "SAAS-昨日的商户交易对账定时任务出错!");
            throw new BusinessException(EResultEnum.ERROR.getCode());
        }
    }

    /**
     * 分页查询交易对账总表信息
     *
     * @param tradeCheckAccountDTO 查询DTO
     * @return 总表信息集合
     */
    @Override
    public PageInfo<TradeCheckAccount> pageFindTradeCheckAccount(TradeCheckAccountDTO tradeCheckAccountDTO) {
        return new PageInfo<>(tradeCheckAccountMapper.pageFindTradeCheckAccount(tradeCheckAccountDTO));
    }

    /**
     * 分页查询交易对账详细表信息
     *
     * @param tradeCheckAccountDTO 查询DTO
     * @return 详情表信息集合
     */
    @Override
    public PageInfo<TradeCheckAccountDetail> pageFindTradeCheckAccountDetail(TradeCheckAccountDTO tradeCheckAccountDTO) {
        return new PageInfo<>(tradeCheckAccountDetailMapper.pageFindTradeCheckAccountDetail(tradeCheckAccountDTO));
    }

    /**
     * 导出商户交易对账单
     *
     * @param tradeCheckAccountDTO 查询DTO
     * @return 对账单
     */
    @Override
    public ExportTradeAccountVO exportTradeCheckAccount(TradeCheckAccountDTO tradeCheckAccountDTO) {
        //获取当前请求语言
        tradeCheckAccountDTO.setLanguage(auditorProvider.getLanguage());
        //时间为空,默认为昨天
        if (StringUtils.isEmpty(tradeCheckAccountDTO.getStartDate()) && StringUtils.isEmpty(tradeCheckAccountDTO.getEndDate())) {
            //昨日日期
            String yesterday = DateToolUtils.getYesterday();
            tradeCheckAccountDTO.setStartDate(yesterday);
            tradeCheckAccountDTO.setEndDate(yesterday);
        }
        //总表信息
        List<TradeCheckAccount> tradeCheckAccountList = tradeCheckAccountMapper.exportTradeCheckAccount(tradeCheckAccountDTO);
        //详细表信息
        List<TradeAccountDetailVO> tradeAccountDetailVOList = tradeCheckAccountDetailMapper.exportTradeCheckAccountDetail(tradeCheckAccountDTO);
        for (TradeAccountDetailVO accountDetail : tradeAccountDetailVOList) {
            for (TradeCheckAccountDetailVO tradeCheckAccountDetailVO : accountDetail.getTradeCheckAccountDetailVOS()) {
                if (AsianWalletConstant.PAYMENT.equals(tradeCheckAccountDetailVO.getTradeType())) {
                    tradeCheckAccountDetailVO.setTradeTypeName("Payment");
                    tradeCheckAccountDetailVO.setTradeStatusName("Payment Success");
                } else if (AsianWalletConstant.REFUND.equals(tradeCheckAccountDetailVO.getTradeType())) {
                    tradeCheckAccountDetailVO.setTradeTypeName("Refund");
                    if (TradeConstant.REFUND_SUCCESS.equals(tradeCheckAccountDetailVO.getRefundStatus())) {
                        tradeCheckAccountDetailVO.setTradeStatusName("Refund Success");
                    } else if (TradeConstant.REFUND_WAIT.equals(tradeCheckAccountDetailVO.getRefundStatus())) {
                        tradeCheckAccountDetailVO.setTradeStatusName("Refund Pending");
                    } else if (TradeConstant.REFUND_FALID.equals(tradeCheckAccountDetailVO.getRefundStatus())) {
                        tradeCheckAccountDetailVO.setTradeStatusName("Refund Fail");
                    }
                }
                if (TradeConstant.FEE_TYPE_RATE.equals(tradeCheckAccountDetailVO.getRateType())) {
                    tradeCheckAccountDetailVO.setRateType("Single Rate");
                } else if (TradeConstant.FEE_TYPE_QUOTA.equals(tradeCheckAccountDetailVO.getRateType())) {
                    tradeCheckAccountDetailVO.setRateType("Single Quota");
                } else {
                    tradeCheckAccountDetailVO.setRateType("");
                }
            }
        }
        ExportTradeAccountVO exportTradeAccountVO = new ExportTradeAccountVO();
        exportTradeAccountVO.setTradeCheckAccounts(tradeCheckAccountList);
        exportTradeAccountVO.setTradeAccountDetailVOS(tradeAccountDetailVOList);
        return exportTradeAccountVO;
    }
}
