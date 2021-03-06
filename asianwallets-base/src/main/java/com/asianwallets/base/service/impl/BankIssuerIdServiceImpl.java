package com.asianwallets.base.service.impl;

import com.asianwallets.base.dao.BankIssuerIdMapper;
import com.asianwallets.base.service.BankIssuerIdService;
import com.asianwallets.common.dto.BankIssuerIdDTO;
import com.asianwallets.common.entity.BankIssuerId;
import com.asianwallets.common.exception.BusinessException;
import com.asianwallets.common.response.EResultEnum;
import com.asianwallets.common.utils.IDS;
import com.asianwallets.common.vo.BankIssuerIdVO;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class BankIssuerIdServiceImpl implements BankIssuerIdService {

    @Autowired
    private BankIssuerIdMapper bankIssuerIdMapper;

    /**
     * 添加银行机构代码映射信息
     *
     * @param username            用户名
     * @param bankIssuerIdDTOList 银行机构代码映射输入实体集合
     * @return 修改条数
     */
    @Override
    @Transactional
    public int addBankIssuerId(String username, List<BankIssuerIdDTO> bankIssuerIdDTOList) {
        List<BankIssuerId> bankIssuerIdList = new ArrayList<>();
        for (BankIssuerIdDTO bankIssuerIdDTO : bankIssuerIdDTOList) {
            //存在相同信息的不添加
            if (bankIssuerIdMapper.selectByChannelCodeAndBankNameAndCurrency(bankIssuerIdDTO.getChannelCode(), bankIssuerIdDTO.getBankName(), bankIssuerIdDTO.getCurrency()) != null) {
                continue;
            }
            BankIssuerId bankIssuerId = new BankIssuerId();
            bankIssuerId.setId(IDS.uuid2());
            bankIssuerId.setBankName(bankIssuerIdDTO.getBankName());
            bankIssuerId.setIssuerId(bankIssuerIdDTO.getIssuerId());
            bankIssuerId.setCurrency(bankIssuerIdDTO.getCurrency());
            bankIssuerId.setChannelCode(bankIssuerIdDTO.getChannelCode());
            bankIssuerId.setCreator(username);
            bankIssuerId.setCreateTime(new Date());
            bankIssuerId.setEnabled(true);
            bankIssuerIdList.add(bankIssuerId);
        }
        if (bankIssuerIdList.size() == 0) {
            throw new BusinessException(EResultEnum.REPEATED_ADDITION.getCode());
        }
        return bankIssuerIdMapper.insertList(bankIssuerIdList);
    }

    /**
     * 修改银行机构代码映射信息
     *
     * @param username        用户名
     * @param bankIssuerIdDTO 银行机构代码映射输入实体
     * @return 修改条数
     */
    @Override
    @Transactional
    public int updateBankIssuerId(String username, BankIssuerIdDTO bankIssuerIdDTO) {
        BankIssuerId bankIssuerId = bankIssuerIdMapper.selectByPrimaryKey(bankIssuerIdDTO.getId());
        if (bankIssuerId == null) {
            log.info("==========【修改银行机构代码映射信息】==========【bankIssuerId不存在】");
            throw new BusinessException(EResultEnum.DATA_IS_NOT_EXIST.getCode());
        }
        bankIssuerId.setIssuerId(bankIssuerIdDTO.getIssuerId());
        bankIssuerId.setUpdateTime(new Date());
        bankIssuerId.setModifier(username);
        return bankIssuerIdMapper.updateByPrimaryKeySelective(bankIssuerId);
    }

    /**
     * 分页查询银行机构代码映射信息
     *
     * @param bankIssuerIdDTO 银行机构代码映射输入实体
     * @return PageInfo<BankIssuerIdVO>
     */
    @Override
    public PageInfo<BankIssuerIdVO> pageFindBankIssuerId(BankIssuerIdDTO bankIssuerIdDTO) {
        bankIssuerIdDTO.setSort("b.create_time");
        return new PageInfo<>(bankIssuerIdMapper.pageFindBankIssuerId(bankIssuerIdDTO));
    }

    /**
     * 导出银行机构代码映射信息
     *
     * @param bankIssuerIdDTO 银行机构代码映射输入实体
     * @return List<BankIssuerIdVO>
     */
    @Override
    public List<BankIssuerIdVO> exportBankIssuerId(BankIssuerIdDTO bankIssuerIdDTO) {
        return bankIssuerIdMapper.exportBankIssuerId(bankIssuerIdDTO);
    }

    /**
     * 根据条件查询银行机构映射信息
     *
     * @param bankIssuerIdDTO 银行机构代码映射输入实体
     * @return BankIssuerId
     */
    @Override
    public BankIssuerId getByTerm(BankIssuerIdDTO bankIssuerIdDTO) {
        return bankIssuerIdMapper.selectByTerm(bankIssuerIdDTO);
    }

    /**
     * 导入银行机构代码映射信息
     *
     * @param bankIssuerIdList 机构代码映射信息集合
     * @return 修改条数
     */
    @Override
    @Transactional
    public int importBankIssuerId(List<BankIssuerId> bankIssuerIdList) {
        return bankIssuerIdMapper.insertList(bankIssuerIdList);
    }
}
