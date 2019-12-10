package com.asianwallets.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.asianwallets.base.dao.*;
import com.asianwallets.base.job.ProductInfoJob;
import com.asianwallets.base.service.MerchantProductService;
import com.asianwallets.common.base.BaseServiceImpl;
import com.asianwallets.common.constant.AsianWalletConstant;
import com.asianwallets.common.constant.TradeConstant;
import com.asianwallets.common.dto.*;
import com.asianwallets.common.entity.*;
import com.asianwallets.common.exception.BusinessException;
import com.asianwallets.common.redis.RedisService;
import com.asianwallets.common.response.BaseResponse;
import com.asianwallets.common.response.EResultEnum;
import com.asianwallets.common.response.ResultUtil;
import com.asianwallets.common.utils.DateToolUtils;
import com.asianwallets.common.utils.IDS;
import com.asianwallets.common.vo.ChaBankRelVO;
import com.asianwallets.common.vo.MerChannelVO;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yx
 * @since 2019-12-09
 */
@Slf4j
@Service
@Transactional
public class MerchantProductServiceImpl extends BaseServiceImpl<MerchantProduct> implements MerchantProductService {

    @Autowired
    private MerchantProductMapper merchantProductMapper;
    @Autowired
    private MerchantProductAuditMapper merchantProductAuditMapper;
    @Autowired
    private MerchantProductHistoryMapper merchantProductHistoryMapper;
    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private MerchantChannelMapper merchantChannelMapper;
    @Autowired
    private ChannelBankMapper channelBankMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private QrtzJobDetailsMapper qrtzJobDetailsMapper;
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private SettleControlMapper settleControlMapper;


    /**
     * @return
     * @Author YangXu
     * @Date 2019/12/9
     * @Descripate 添加商户产品
     **/
    @Override
    public int addMerchantProduct(String name, List<MerchantProductDTO> merchantProductDTOs) {
        List<MerchantProduct> list = Lists.newArrayList();
        List<MerchantProductAudit> listAudit = Lists.newArrayList();

        for (MerchantProductDTO merchantProductDTO : merchantProductDTOs) {
            //查询商户是否已经分配产品
            if (merchantProductMapper.selectCountbyMerIdProId(merchantProductDTO.getMerchantId(), merchantProductDTO.getProductId()) > 0) {
                continue;
            }

            //根据当前商户id获取代理商信息
            Merchant merchant = merchantMapper.selectByPrimaryKey(merchantProductDTO.getMerchantId());
            if (!StringUtils.isEmpty(merchant.getAgentId())) {
                Merchant agentMerchant = merchantMapper.selectByPrimaryKey(merchant.getAgentId());
                MerchantProduct agentMerchantProduct = merchantProductMapper.getMerchantProductByMerIdAndProId(agentMerchant.getId(), merchantProductDTO.getProductId());
                if (agentMerchantProduct != null && agentMerchantProduct.getRateType().equals(agentMerchantProduct.getRateType())) {
                    //单笔定额的场合
                    if (TradeConstant.FEE_TYPE_QUOTA.equals(merchantProductDTO.getRateType())) {
                        if (merchantProductDTO.getRate().compareTo(agentMerchantProduct.getRate()) == -1) {
                            throw new BusinessException(EResultEnum.RATE_IS_ILLEGAL.getCode());
                        }
                    } else if (TradeConstant.FEE_TYPE_RATE.equals(merchantProductDTO.getRateType())) {
                        //单笔费率
                        if (merchantProductDTO.getRate().compareTo(agentMerchantProduct.getRate()) == -1 ||
                                merchantProductDTO.getMinTate().compareTo(agentMerchantProduct.getMinTate()) == -1 ||
                                merchantProductDTO.getMaxTate().compareTo(agentMerchantProduct.getMaxTate()) == -1) {
                            throw new BusinessException(EResultEnum.RATE_IS_ILLEGAL.getCode());
                        }
                    }
                } else if (agentMerchantProduct != null && !agentMerchantProduct.getRateType().equals(agentMerchantProduct.getRateType())) {
                    //商户的产品费率类型和代理商的产品费率类型不一致
                    throw new BusinessException(EResultEnum.RATE_TYPE_IS_DIFFERENT.getCode());
                }
            }

            String id = IDS.uuid2();
            MerchantProduct merchantProduct = new MerchantProduct();
            BeanUtils.copyProperties(merchantProductDTO, merchantProduct);
            merchantProduct.setId(id);
            merchantProduct.setCreateTime(new Date());
            merchantProduct.setCreator(name);
            merchantProduct.setEnabled(false);
            merchantProduct.setAuditStatus(TradeConstant.AUDIT_WAIT);

            MerchantProductAudit merchantProductAudit = new MerchantProductAudit();
            BeanUtils.copyProperties(merchantProduct, merchantProductAudit);

            list.add(merchantProduct);
            listAudit.add(merchantProductAudit);

        }
        if (list != null && list.size() > 0) {
            merchantProductMapper.insertList(list);
            return merchantProductAuditMapper.insertList(listAudit);
        } else {
            throw new BusinessException(EResultEnum.PAYMENTMODE_EXIST.getCode());
        }
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/12/9
     * @Descripate 商户分配通道
     **/
    @Override
    public int allotMerProductChannel(String username, MerProDTO merProDTO) {
        log.info("----------------- 商户分配通道 ---------------- username : {},merProDTO : {} ", username, JSONObject.toJSONString(merProDTO));
        //必填的check
        if (merProDTO == null) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        //必填的check
        if (StringUtils.isEmpty(merProDTO.getMerchantId()) || StringUtils.isEmpty(merProDTO.getProductList()) || merProDTO.getProductList().size() == 0) {
            throw new BusinessException(EResultEnum.PARAMETER_IS_NOT_PRESENT.getCode());
        }
        List<MerchantChannel> list = Lists.newArrayList();
        for (ProdChannelDTO prodChannelDTO : merProDTO.getProductList()) {
            //根据商户id与产品id查询商户产品中间表id
            MerchantProduct merchantProduct = merchantProductMapper.getMerchantProductByMerIdAndProId(merProDTO.getMerchantId(), prodChannelDTO.getProductId());
            List<MerchantChannel> list1 = merchantChannelMapper.selectByMerProId(merchantProduct.getId());
            //根据商户产品中间表id删除机构通道信息
            merchantChannelMapper.deleteByMerProId(merchantProduct.getId());
            for (ChannelInfoDTO channelInfoDTO : prodChannelDTO.getChannelList()) {
                for (BankInfoDTO bankInfoDTO : channelInfoDTO.getBankList()) {
                    ChaBankRelVO chaBankRelVO = channelBankMapper.getInfoByCIdAndBId(channelInfoDTO.getChannelId(), bankInfoDTO.getBankId());
                    MerchantChannel merchantChannel = new MerchantChannel();
                    merchantChannel.setId(IDS.uuid2());
                    merchantChannel.setMerProId(merchantProduct.getId());
                    //通道银行id
                    merchantChannel.setChaBanId(chaBankRelVO.getChabankId());
                    merchantChannel.setCreateTime(new Date());
                    merchantChannel.setCreator(username);
                    merchantChannel.setSort(chaBankRelVO.getSort());
                    merchantChannel.setEnabled(true);
                    boolean flag = true;
                    for (MerchantChannel mc : list1) {
                        if (merchantChannel.getMerProId().equals(mc.getMerProId()) && merchantChannel.getChaBanId().equals(mc.getChaBanId())) {
                            flag = false;
                            list.add(mc);
                        }
                    }
                    if (flag) {
                        list.add(merchantChannel);
                    }
                }
            }
        }
        merchantChannelMapper.insertList(list);
        for (MerchantChannel merchantChannel : list) {
            //审核通过后将新增和修改的通道信息添加的redis里
            List<String> chaBanIds = merchantChannelMapper.selectChannelCodeByMerProId(merchantChannel.getMerProId());
            try {
                redisService.set(AsianWalletConstant.MERCHANTCHANNEL_CACHE_KEY.concat("_").concat(merchantChannel.getMerProId()),
                        JSON.toJSONString(chaBanIds));
            } catch (Exception e) {
                log.error("审核通过后将新增和修改的通道信息添加的redis里：", e.getMessage());
                throw new BusinessException(EResultEnum.ERROR_REDIS_UPDATE.getCode());
            }
        }
        return 0;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/12/9
     * @Descripate 修改商户产品
     **/
    @Override
    public int updateMerchantProduct(String name, MerchantProductDTO merchantProductDTO) {
        Date date = merchantProductDTO.getEffectTime();
        Date date1 = DateToolUtils.addMinute(new Date(), 30);
        if (date.getTime() < date1.getTime()) {
            throw new BusinessException(EResultEnum.EFFECTTIME_IS_ILLEGAL.getCode());
        }
        if (qrtzJobDetailsMapper.getCountByInsProId(merchantProductDTO.getMerProId().concat("_PRODUCT_INFO")) > 0) {
            throw new BusinessException(EResultEnum.AUDIT_INFO_EXIENT.getCode());
        }
        int num = 0;
        MerchantProductAudit oldMerchantProductAudit = merchantProductAuditMapper.selectByPrimaryKey(merchantProductDTO.getMerProId());
        MerchantProduct oldMerPro = merchantProductMapper.selectByPrimaryKey(merchantProductDTO.getMerProId());
        MerchantProductAudit merchantProductAudit = new MerchantProductAudit();
        //如果该商户已经不存在或者禁用的话，是不允许进行修改的
        Merchant merchant = merchantMapper.selectByPrimaryKey(oldMerPro.getMerchantId());
        if (merchant == null) {//商户信息不存在
            throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());//商户信息不存在
        }
        //商户已禁用
        if (!merchant.getEnabled()) {
            throw new BusinessException(EResultEnum.INSTITUTION_IS_DISABLE.getCode());//商户已禁用
        }
        //根据当前商户id获取代理商信息
        if (!StringUtils.isEmpty(merchant.getAgentId())) {
            //代理商信息
            Merchant agentMerchant = merchantMapper.selectByPrimaryKey(merchant.getAgentId());
            MerchantProduct agentMerchantProduct = merchantProductMapper.getMerchantProductByMerIdAndProId(agentMerchant.getId(), merchantProductDTO.getProductId());
            if (agentMerchantProduct != null && agentMerchantProduct.getRateType().equals(agentMerchantProduct.getRateType())) {
                //单笔定额的场合
                if (TradeConstant.FEE_TYPE_QUOTA.equals(merchantProductDTO.getRateType())) {
                    if (merchantProductDTO.getRate().compareTo(agentMerchantProduct.getRate()) == -1) {
                        throw new BusinessException(EResultEnum.RATE_IS_ILLEGAL.getCode());
                    }
                } else if (TradeConstant.FEE_TYPE_RATE.equals(merchantProductDTO.getRateType())) {
                    //单笔费率
                    if (merchantProductDTO.getRate().compareTo(agentMerchantProduct.getRate()) == -1 ||
                            merchantProductDTO.getMinTate().compareTo(agentMerchantProduct.getMinTate()) == -1 ||
                            merchantProductDTO.getMaxTate().compareTo(agentMerchantProduct.getMaxTate()) == -1) {
                        throw new BusinessException(EResultEnum.RATE_IS_ILLEGAL.getCode());
                    }
                }
            } else if (agentMerchantProduct != null && !agentMerchantProduct.getRateType().equals(agentMerchantProduct.getRateType())) {
                //商户的产品费率类型和代理商的产品费率类型不一致
                throw new BusinessException(EResultEnum.RATE_TYPE_IS_DIFFERENT.getCode());
            }
        }

        if (oldMerchantProductAudit == null) {
            BeanUtils.copyProperties(oldMerPro, merchantProductAudit);
            merchantProductAudit.setTradeDirection(merchantProductDTO.getTradeDirection());
            merchantProductAudit.setPayType(merchantProductDTO.getPayType());
            merchantProductAudit.setProductId(merchantProductDTO.getProductId());
            merchantProductAudit.setProductAbbrev(merchantProductDTO.getProductAbbrev());
            merchantProductAudit.setRateType(merchantProductDTO.getRateType());
            merchantProductAudit.setRate(merchantProductDTO.getRate());
            //费率类型为单笔费率的场合才有费率最小值和费率最大值
            if (merchantProductDTO.getRateType() != null && TradeConstant.FEE_TYPE_RATE.equals(merchantProductDTO.getRateType())) {
                merchantProductAudit.setMaxTate(merchantProductDTO.getMaxTate());
                merchantProductAudit.setMinTate(merchantProductDTO.getMinTate());
            }
            merchantProductAudit.setAddValue(merchantProductDTO.getAddValue());
            merchantProductAudit.setFloatRate(merchantProductDTO.getFloatRate());
            merchantProductAudit.setRefundDefault(merchantProductDTO.getRefundDefault());
            if (merchantProductDTO.getRefundDefault()) {
                merchantProductAudit.setRefundRateType(merchantProductDTO.getRefundRateType());
                merchantProductAudit.setRefundRate(merchantProductDTO.getRefundRate());
                if (merchantProductDTO.getRefundRateType() != null && TradeConstant.FEE_TYPE_RATE.equals(merchantProductDTO.getRefundRateType())) {
                    merchantProductAudit.setRefundMaxTate(merchantProductDTO.getRefundMaxTate());
                    merchantProductAudit.setRefundMinTate(merchantProductDTO.getRefundMinTate());
                }
                merchantProductAudit.setRefundAddValue(merchantProductDTO.getRefundAddValue());
            }
            merchantProductAudit.setDividedMode(merchantProductDTO.getDividedMode());
            merchantProductAudit.setDividedRatio(merchantProductDTO.getDividedRatio());
            merchantProductAudit.setDividedRatio(merchantProductDTO.getDividedRatio());
            merchantProductAudit.setAuditStatus(TradeConstant.AUDIT_WAIT);
            merchantProductAudit.setEffectTime(merchantProductDTO.getEffectTime());
            merchantProductAudit.setCreateTime(new Date());
            merchantProductAudit.setCreator(oldMerPro.getCreator());
            merchantProductAudit.setModifier(name);
            merchantProductAudit.setUpdateTime(oldMerPro.getCreateTime());
            num = merchantProductAuditMapper.insert(merchantProductAudit);

        } else if (oldMerchantProductAudit.getAuditStatus() == TradeConstant.AUDIT_FAIL || oldMerchantProductAudit.getAuditStatus() == TradeConstant.AUDIT_SUCCESS) {

            BeanUtils.copyProperties(oldMerPro, merchantProductAudit);
            merchantProductAudit.setTradeDirection(merchantProductDTO.getTradeDirection());
            merchantProductAudit.setPayType(merchantProductDTO.getPayType());
            merchantProductAudit.setProductId(merchantProductDTO.getProductId());
            merchantProductAudit.setProductAbbrev(merchantProductDTO.getProductAbbrev());
            merchantProductAudit.setRateType(merchantProductDTO.getRateType());
            merchantProductAudit.setRate(merchantProductDTO.getRate());
            //费率类型为单笔费率的场合才有费率最小值和费率最大值
            if (merchantProductDTO.getRateType() != null && TradeConstant.FEE_TYPE_RATE.equals(merchantProductDTO.getRateType())) {
                merchantProductAudit.setMaxTate(merchantProductDTO.getMaxTate());
                merchantProductAudit.setMinTate(merchantProductDTO.getMinTate());
            }
            merchantProductAudit.setAddValue(merchantProductDTO.getAddValue());
            merchantProductAudit.setFloatRate(merchantProductDTO.getFloatRate());
            merchantProductAudit.setRefundDefault(merchantProductDTO.getRefundDefault());
            if (merchantProductDTO.getRefundDefault()) {
                merchantProductAudit.setRefundRateType(merchantProductDTO.getRefundRateType());
                merchantProductAudit.setRefundRate(merchantProductDTO.getRefundRate());
                if (merchantProductDTO.getRefundRateType() != null && TradeConstant.FEE_TYPE_RATE.equals(merchantProductDTO.getRefundRateType())) {
                    merchantProductAudit.setRefundMaxTate(merchantProductDTO.getRefundMaxTate());
                    merchantProductAudit.setRefundMinTate(merchantProductDTO.getRefundMinTate());
                }
                merchantProductAudit.setRefundAddValue(merchantProductDTO.getRefundAddValue());
            }
            merchantProductAudit.setDividedMode(merchantProductDTO.getDividedMode());
            merchantProductAudit.setDividedRatio(merchantProductDTO.getDividedRatio());
            merchantProductAudit.setDividedRatio(merchantProductDTO.getDividedRatio());
            merchantProductAudit.setAuditStatus(TradeConstant.AUDIT_WAIT);
            merchantProductAudit.setEffectTime(merchantProductDTO.getEffectTime());
            merchantProductAudit.setModifier(name);
            merchantProductAudit.setCreateTime(new Date());
            num = merchantProductAuditMapper.updateByPrimaryKeySelective(merchantProductAudit);

        } else if (oldMerchantProductAudit.getAuditStatus() == TradeConstant.AUDIT_WAIT || oldMerchantProductAudit.getAuditStatus() == null) {
            throw new BusinessException(EResultEnum.AUDIT_INFO_EXIENT.getCode());
        }
        return num;
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/12/10
     * @Descripate 批量审核商户产品
     **/
    @Override
    public BaseResponse auditMerchantProduct(String username, AuaditProductDTO auaditProductDTO) {
        BaseResponse baseResponse = new BaseResponse();
        List<String> list = auaditProductDTO.getMerProId();
        int num = 0;
        for (String merProId : list) {
            MerchantProduct oldMerchantProduct = merchantProductMapper.selectByPrimaryKey(merProId);
            //如果该商户已经不存在或者禁用的话，是不允许进行修改的
            Merchant merchant = merchantMapper.selectByPrimaryKey(oldMerchantProduct.getMerchantId());
            if (merchant == null) {//商户信息不存在
                throw new BusinessException(EResultEnum.INSTITUTION_NOT_EXIST.getCode());//商户信息不存在
            }
            //商户已禁用
            if (!merchant.getEnabled()) {
                throw new BusinessException(EResultEnum.INSTITUTION_IS_DISABLE.getCode());//商户已禁用
            }
            MerchantProductAudit oldMerchantProductAudit = merchantProductAuditMapper.selectByPrimaryKey(merProId);
            if (oldMerchantProductAudit.getEnabled()) {
                //如果审核记录里的启用状态为true，则为非初次添加
                if (auaditProductDTO.enabled) {
                    //非初次添加信息审核通过
                    //构建job信息
                    String name = merProId;
                    String group = merProId.concat("_PRODUCT_INFO");
                    JobDataMap jobDataMap = new JobDataMap();
                    jobDataMap.put("merProId", merProId);
                    JobDetail jobDetail = JobBuilder.newJob(ProductInfoJob.class).withIdentity(name, group).setJobData(jobDataMap).build();
                    //表达式调度构建器(即任务执行的时间)
                    Date runDate = oldMerchantProductAudit.getEffectTime();
                    if (runDate == null) {
                        baseResponse.setCode(EResultEnum.EFFECTTIME_IS_NULL.getCode());//生效时间不能为空
                        return baseResponse;
                    }
                    if (runDate.compareTo(new Date()) < 0) {
                        MerchantProductHistory merchantProductHistory = new MerchantProductHistory();
                        BeanUtils.copyProperties(oldMerchantProductAudit, merchantProductHistory);
                        merchantProductHistory.setId(IDS.uuid2());
                        merchantProductHistory.setMerchantProductId(oldMerchantProductAudit.getId());
                        merchantProductHistory.setAuditStatus(TradeConstant.AUDIT_FAIL);
                        merchantProductHistory.setAuditRemark("生效时间不合法");
                        merchantProductHistoryMapper.insert(merchantProductHistory);
                        //原记录信息审核状态记录失败
                        oldMerchantProductAudit.setAuditStatus(TradeConstant.AUDIT_FAIL);
                        //oldInstitutionProductAudit.setUpdateTime(new Date());
                        oldMerchantProductAudit.setModifier(name);
                        oldMerchantProductAudit.setAuditRemark(auaditProductDTO.getRemarks());
                        merchantProductAuditMapper.updateByPrimaryKeySelective(oldMerchantProductAudit);
                        baseResponse.setCode(EResultEnum.EFFECTTIME_IS_ILLEGAL.getCode());//生效时间不合法
                        return baseResponse;
                    }
                    //更改审核信息状态
                    oldMerchantProductAudit.setAuditStatus(TradeConstant.AUDIT_SUCCESS);
                    merchantProductAuditMapper.updateByPrimaryKeySelective(oldMerchantProductAudit);
                    //根据配置动态生成cron表达式
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(runDate);
                    String yyyy = String.valueOf(calendar.get(Calendar.YEAR));
                    String mm = String.valueOf(calendar.get(Calendar.MONTH) + 1);
                    String dd = String.valueOf(calendar.get(Calendar.DATE));
                    String HH = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
                    String minute = String.valueOf(calendar.get(Calendar.MINUTE));
                    String ss = String.valueOf(calendar.get(Calendar.SECOND));
                    //生成 eg:【30 45 10 20 8 2018】格式 固定时间执行任务
                    String cronExpression = ss.concat(" ").concat(minute)
                            .concat(" ").concat(HH)
                            .concat(" ").concat(dd)
                            .concat(" ").concat(mm)
                            .concat(" ").concat("?")
                            .concat(" ").concat(yyyy);
                    CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
                    //按新的cronExpression表达式构建一个新的trigger
                    CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(name, group)
                            .withSchedule(scheduleBuilder).build();
                    try {
                        scheduler.scheduleJob(jobDetail, trigger);
                    } catch (SchedulerException e) {
                        e.printStackTrace();
                    }

                } else {
                    //非初次添加信息审核不通过
                    oldMerchantProductAudit.setAuditStatus(TradeConstant.AUDIT_FAIL);
                    //oldInstitutionProductAudit.setUpdateTime(new Date());
                    oldMerchantProductAudit.setModifier(username);
                    oldMerchantProductAudit.setAuditRemark(auaditProductDTO.getRemarks());
                    merchantProductAuditMapper.updateByPrimaryKeySelective(oldMerchantProductAudit);

                    MerchantProductHistory merchantProductHistory = new MerchantProductHistory();
                    BeanUtils.copyProperties(oldMerchantProductAudit, merchantProductHistory);
                    merchantProductHistory.setId(IDS.uuid2());
                    merchantProductHistory.setMerchantProductId(oldMerchantProductAudit.getId());
                    merchantProductHistory.setAuditStatus(TradeConstant.AUDIT_FAIL);
                    merchantProductHistoryMapper.insert(merchantProductHistory);
                }
            } else {
                //如果审核记录里的启用状态为false ,则为初次添加状态
                if (auaditProductDTO.enabled) {
                    //初次添加信息审核通过 更改主表，审核表审核信息状态
                    MerchantProduct merchantProduct = merchantProductMapper.selectByPrimaryKey(merProId);
                    merchantProduct.setId(merProId);
                    merchantProduct.setAuditStatus(TradeConstant.AUDIT_SUCCESS);
                    merchantProduct.setModifier(username);
                    merchantProduct.setEnabled(true);
                    merchantProduct.setUpdateTime(new Date());

                    MerchantProductAudit merchantProductAudit = new MerchantProductAudit();
                    merchantProductAudit.setId(merProId);
                    merchantProductAudit.setAuditStatus(TradeConstant.AUDIT_SUCCESS);
                    merchantProductAudit.setModifier(username);
                    //institutionProductAudit.setUpdateTime(new Date());
                    merchantProductAudit.setEnabled(true);
                    merchantProduct.setCreateTime(merchantProductAudit.getUpdateTime());

                    merchantProductMapper.updateByPrimaryKeySelective(merchantProduct);
                    merchantProductAuditMapper.updateByPrimaryKeySelective(merchantProductAudit);

                    //添加账户
                    Account account = new Account();
                    //账户关联表 自动结算开关和最小提现金额
                    SettleControl settleControl = new SettleControl();
                    String currency = productMapper.selectByPrimaryKey(merchantProductMapper.selectByPrimaryKey(merProId).getProductId()).getCurrency();
                    if (accountMapper.getCountByinstitutionIdAndCurry(oldMerchantProduct.getMerchantId(), currency) == 0) {
                        account.setAccountCode(IDS.uniqueID().toString());
                        account.setMerchantId(oldMerchantProduct.getMerchantId());
                        account.setMerchantName(merchantMapper.selectByPrimaryKey(oldMerchantProduct.getMerchantId()).getCnName());
                        account.setCurrency(currency);//币种
                        account.setId(IDS.uuid2());
                        account.setSettleBalance(BigDecimal.ZERO);//默认结算金额为0
                        account.setClearBalance(BigDecimal.ZERO);//默认清算金额为0
                        account.setFreezeBalance(BigDecimal.ZERO);//默认冻结金额为0
                        account.setEnabled(true);//产品审核通过以后默认币种的状态是启用的
                        account.setCreateTime(new Date());//创建时间
                        account.setCreator(username);//创建人
                        account.setRemark("产品信息审核通过后自动创建币种的账户");
                        //账户关联表id
                        settleControl.setId(IDS.uuid2());
                        //账户id
                        settleControl.setAccountId(account.getId());
                        //设置最小提现金额为0
                        settleControl.setMinSettleAmount(BigDecimal.ZERO);
                        settleControl.setCreateTime(new Date());
                        settleControl.setEnabled(true);
                        settleControl.setCreator(username);
                        settleControl.setRemark("产品信息审核通过后自动创建币种的结算控制信息");
                        if (accountMapper.insertSelective(account) > 0 && settleControlMapper.insertSelective(settleControl) > 0) {
                            redisService.set(AsianWalletConstant.ACCOUNT_CACHE_KEY.concat("_").concat(oldMerchantProduct.getMerchantId()).concat("_").concat(currency), JSON.toJSONString(account));
                        }
                    }
                    //若审核表限额状态为成功,删除审核记录
                    if (oldMerchantProductAudit.getAuditStatus() == TradeConstant.AUDIT_SUCCESS) {
                        merchantProductAuditMapper.deleteByPrimaryKey(oldMerchantProduct.getMerchantId());
                    }
                    //审核通过后将新增和修改的机构产品信息添加的redis里
                    try {
                        redisService.set(AsianWalletConstant.MERCHANTPRODUCT_CACHE_KEY.concat("_").concat(merchantProduct.getMerchantId().concat("_").concat(merchantProduct.getProductId())), JSON.toJSONString(merchantProduct));
                    } catch (Exception e) {
                        log.error("审核通过后将新增和修改的机构产品信息添加的redis里：" + e.getMessage());
                        throw new BusinessException(EResultEnum.ERROR_REDIS_UPDATE.getCode());
                    }

                } else {
                    //初次添加审核不通过
                    MerchantProduct merchantProduct = new MerchantProduct();
                    merchantProduct.setId(merProId);
                    merchantProduct.setAuditStatus(TradeConstant.AUDIT_FAIL);
                    merchantProduct.setAuditRemark(auaditProductDTO.getRemarks());
                    merchantProduct.setModifier(username);
                    merchantProduct.setUpdateTime(new Date());
                    num = merchantProductMapper.updateByPrimaryKeySelective(merchantProduct);

                    MerchantProductAudit merchantProductAudit = new MerchantProductAudit();
                    merchantProductAudit.setId(merProId);
                    merchantProductAudit.setAuditStatus(TradeConstant.AUDIT_FAIL);
                    merchantProductAudit.setAuditRemark(auaditProductDTO.getRemarks());
                    merchantProductAudit.setModifier(username);
                    //institutionProductAudit.setUpdateTime(new Date());
                    merchantProductAuditMapper.updateByPrimaryKeySelective(merchantProductAudit);

                    MerchantProductHistory merchantProductAuditHistory = new MerchantProductHistory();
                    BeanUtils.copyProperties(oldMerchantProductAudit, merchantProductAuditHistory);
                    merchantProductAuditHistory.setId(IDS.uuid2());
                    merchantProductAuditHistory.setMerchantProductId(oldMerchantProductAudit.getId());
                    merchantProductAuditHistory.setAuditStatus(TradeConstant.AUDIT_FAIL);
                    merchantProductHistoryMapper.insert(merchantProductAuditHistory);
                }
            }
        }
        return ResultUtil.success(num);
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/12/10
     * @Descripate 分页查询商户产品信息
     **/
    @Override
    public PageInfo<MerchantProduct> pageFindMerProduct(MerchantProductDTO merchantProductDTO) {
        return new PageInfo<MerchantProduct>(merchantProductMapper.pageFindMerProduct(merchantProductDTO));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/12/10
     * @Descripate 分页查询商户审核产品信息
     **/
    @Override
    public PageInfo<MerchantProductAudit> pageFindMerProductAudit(MerchantProductDTO merchantProductDTO) {
        return new PageInfo<MerchantProductAudit>(merchantProductAuditMapper.pageFindMerProductAudit(merchantProductDTO));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/12/10
     * @Descripate 根据产品Id查询商户产品详情
     **/
    @Override
    public MerchantProduct getMerProductById(String merProductId) {
        return merchantProductMapper.selectByPrimaryKey(merProductId);
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/12/10
     * @Descripate 根据Id查询商户产品审核详情
     **/
    @Override
    public MerchantProductAudit getMerProductAuditById(String merProductId) {
        return merchantProductAuditMapper.selectByPrimaryKey(merProductId);
    }


    /**
     * @return
     * @Author YangXu
     * @Date 2019/12/10
     * @Descripate 分页查询商户产品通道管理信息
     **/
    @Override
    public PageInfo<MerChannelVO> pageFindMerProChannel(SearchChannelDTO searchChannelDTO) {
        return new PageInfo<MerChannelVO>(merchantChannelMapper.pageFindMerProChannel(searchChannelDTO));
    }

    /**
     * @return
     * @Author YangXu
     * @Date 2019/12/10
     * @Descripate 修改机构通道
     **/
    @Override
    public int updateMerchantChannel(String username, BatchUpdateSortDTO batchUpdateSort) {
        int num = 0;
        if (StringUtils.isEmpty(batchUpdateSort.getEnabled()) && StringUtils.isEmpty(batchUpdateSort.getSort())) {
            return num;
        }
        //这两个参数不为空,代表这是条需要修改的数据
        MerchantChannel merchantChannel = merchantChannelMapper.selectByPrimaryKey(batchUpdateSort.getMerChannelId());
        if (!StringUtils.isEmpty(batchUpdateSort.getSort())) {
            merchantChannel.setSort(batchUpdateSort.getSort());
        }
        if (!StringUtils.isEmpty(batchUpdateSort.getEnabled())) {
            merchantChannel.setEnabled(batchUpdateSort.getEnabled());
        }
        merchantChannel.setUpdateTime(new Date());
        merchantChannel.setModifier(username);
        num = merchantChannelMapper.updateByPrimaryKeySelective(merchantChannel);
        List<String> list = merchantChannelMapper.selectChannelCodeByInsProId(merchantChannel.getMerProId());
        //同步Redis
        redisService.set(AsianWalletConstant.MERCHANTCHANNEL_CACHE_KEY.concat("_").concat(merchantChannel.getMerProId()), JSON.toJSONString(list));
        return num;
    }
}