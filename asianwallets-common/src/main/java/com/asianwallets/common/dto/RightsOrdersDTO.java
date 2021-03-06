package com.asianwallets.common.dto;
import com.asianwallets.common.base.BasePageHelper;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @description: 权益核销分页查询
 * @author: YangXu
 * @create: 2019-12-30 14:16
 **/
@Data
public class RightsOrdersDTO extends BasePageHelper {

    @ApiModelProperty(value = "订单id")
    private String id;

    @ApiModelProperty(value = "平台名称")
    private String systemName;

    @ApiModelProperty(value = "权益类型")
    private Byte rightsType;

    @ApiModelProperty(value = "核销状态")
    private Byte status;

    @ApiModelProperty(value = "商户编号")
    private String merchantId;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "机构编号")
    private String institutionId;

    @ApiModelProperty(value = "机构名称")
    private String institutionName;

    @ApiModelProperty(value = "商户订单号")
    private String orderNo;

    @ApiModelProperty(value = "票券编号")
    private String ticketId;

    @ApiModelProperty(value = "开始时间")
    private String startDate;

    @ApiModelProperty(value = "结束时间")
    private String endDate;

    @ApiModelProperty(value = "签名类型")
    private String signType;

    @ApiModelProperty(value = "签名")
    private String sign;
}
