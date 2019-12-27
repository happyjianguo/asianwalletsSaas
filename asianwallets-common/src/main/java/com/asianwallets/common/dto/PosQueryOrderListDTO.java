package com.asianwallets.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "POS机查询订单接口输入实体", description = "POS机查询订单接口输入实体")
public class PosQueryOrderListDTO {

    @NotNull(message = "50002")
    @ApiModelProperty(value = "商户编号")
    private String merchantId;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "设备编号")
    private String imei;

    @NotNull(message = "50002")
    @ApiModelProperty(value = "操作员ID")
    private String operatorId;

    @NotNull(message = "52008")
    @ApiModelProperty(value = "签名")
    private String sign;

    @ApiModelProperty(value = "系统订单流水号")
    private String referenceNo;

    @ApiModelProperty(value = "机构订单号")
    private String orderNo;

    @ApiModelProperty(value = "交易状态")
    private Byte txnStatus;

    @ApiModelProperty(value = "退款状态")
    private Byte refundStatus;

    @ApiModelProperty(value = "交易开始时间")
    private String startDate;

    @ApiModelProperty(value = "交易结束时间")
    private String endDate;

    @ApiModelProperty(value = "语言")
    private String language;

    @ApiModelProperty(value = "页码")
    public Integer pageNum;

    @ApiModelProperty(value = "每页条数")
    public Integer pageSize;
}