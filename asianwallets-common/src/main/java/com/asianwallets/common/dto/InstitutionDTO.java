package com.asianwallets.common.dto;

import com.asianwallets.common.base.BasePageHelper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description:
 * @author: YangXu
 * @create: 2019-11-25 10:35
 **/
@Data
@ApiModel(value = "机构实体", description = "机构实体")
public class InstitutionDTO extends BasePageHelper {

    /**
     * 机构id
     */
    @ApiModelProperty(value = "机构id")
    private String institutionId;

    /**
     * 机构logo
     */
    @ApiModelProperty(value = "机构logo")
    private String institutionLogo;

    /**
     * 机构中文名称
     */
    @ApiModelProperty(value = "机构中文名称")
    private String cnName;
    /**
     * 机构英文名称
     */
    @ApiModelProperty(value = "机构英文名称")
    private String enName;
    /**
     * 国别类型 0 境外 1 中国境内
     */
    @ApiModelProperty(value = "国别类型")
    private Integer countryType;
    /**
     * 所属国家
     */
    @ApiModelProperty(value = "所属国家")
    private String country;
    /**
     * 公司注册号
     */
    @ApiModelProperty(value = "公司注册号")
    private String companyRegistNumber;
    /**
     * 公司证件有效期
     */
    @ApiModelProperty(value = "公司证件有效期")
    private String companyValidity;
    /**
     * 法人证件编号
     */
    @ApiModelProperty(value = "公司证件有效期")
    private String legalPassportCode;

    /**
     * 拓展销售
     */
    @ApiModelProperty(value = "拓展销售")
    private String developSales;

    /**
     * 证件有效期
     */
    @ApiModelProperty(value = "证件有效期")
    private String legalPassportValidity;
    /**
     * 国家区号
     */
    @ApiModelProperty(value = "国家区号")
    private String countryCode;
    /**
     * 机构地址
     */
    @ApiModelProperty(value = "机构地址")
    private String institutionAdress;
    /**
     * 法人证件照片
     */
    @ApiModelProperty(value = "法人证件照片")
    private String legalPassportImg;
    /**
     * 机构协议
     */
    @ApiModelProperty(value = "机构协议")
    private String institutionContract;
    /**
     * 公司章程
     */
    @ApiModelProperty(value = "公司章程")
    private String companyArticles;
    /**
     * 企业证件
     */
    @ApiModelProperty(value = "企业证件")
    private String businessCertificate;
    /**
     * 机构电话
     */
    @ApiModelProperty(value = "机构电话")
    private String institutionPhone;
    /**
     * 法人姓名
     */
    @ApiModelProperty(value = "法人姓名")
    private String legalName;
    /**
     * 经营类目
     */
    @ApiModelProperty(value = "经营类目")
    private String businessCategory;
    /**
     * 行业许可
     */
    @ApiModelProperty(value = "行业许可")
    private String businessLicense;
    /**
     * 行业类别
     */
    @ApiModelProperty(value = "行业类别")
    private String industryCategory;
    /**
     * 机构邮编
     */
    @ApiModelProperty(value = "机构邮编")
    private String institutionPostalCode;
    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱")
    private String institutionEmail;
    /**
     * 机构网站url
     */
    @ApiModelProperty(value = "机构网站url")
    private String institutionWebUrl;
    /**
     * 联系人地址
     */
    @ApiModelProperty(value = "联系人地址")
    private String contactAddress;
    /**
     * 联系人
     */
    @ApiModelProperty(value = "联系人")
    private String contactPeople;
    /**
     * 联系人电话
     */
    @ApiModelProperty(value = "联系人电话")
    private String contactPhone;
    /**
     * 审核状态 1-待审核 2-审核通过 3-审核不通过
     */
    @ApiModelProperty(value = "审核状态")
    private Integer auditStatus;

    @ApiModelProperty(value = "是否支持退款")
    private Boolean supportRefundState;

    @ApiModelProperty(value = "退款方式")//1-API接口 2-人工退款
    private Byte refundMethod;

    @ApiModelProperty(value = "是否开通DCC")
    private Boolean dcc;

    @ApiModelProperty(value = "是否开通异步通知")
    private Boolean asynNotice;

    @ApiModelProperty(value = "语言")
    private String language;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;

    @ApiModelProperty(value = "起始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

}
