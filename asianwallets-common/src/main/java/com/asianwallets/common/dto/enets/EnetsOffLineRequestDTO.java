package com.asianwallets.common.dto.enets;

import com.asianwallets.common.entity.Channel;
import com.asianwallets.common.entity.Orders;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @description: Enets线下扫码实体
 * @author: YangXu
 * @create: 2019-06-13 13:45
 **/
@Data
@ApiModel(value = "enets线下扫码请求实体", description = "enets线下扫码请求实体")
public class EnetsOffLineRequestDTO {

    private EnetsSMRequestDTO requestJsonDate;

    private String apiSecret;

    private String apiKeyId;

    private Orders orders;

    private Channel channel;


    public EnetsOffLineRequestDTO() {
    }

    public EnetsOffLineRequestDTO(EnetsSMRequestDTO enetsSMRequestDTO, Orders orders, Channel channel) {
        this.requestJsonDate = enetsSMRequestDTO;
        this.apiSecret = "21296dd3-5bf6-4dfc-b8a2-03fbcc213b7b";
        this.apiKeyId = "b027dacd-1c13-4916-8b93-38fae6be2f80";
        this.orders = orders;
        this.channel = channel;
    }
}
