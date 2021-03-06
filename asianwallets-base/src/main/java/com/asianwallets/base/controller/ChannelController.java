package com.asianwallets.base.controller;

import com.asianwallets.base.service.ChannelService;
import com.asianwallets.common.base.BaseController;
import com.asianwallets.common.dto.AgentChannelsDTO;
import com.asianwallets.common.dto.ChannelDTO;
import com.asianwallets.common.response.BaseResponse;
import com.asianwallets.common.response.ResultUtil;
import com.asianwallets.common.vo.ChannelExportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Api(description = "通道接口")
@RequestMapping("/channel")
public class ChannelController extends BaseController {

    @Autowired
    private ChannelService channelService;

    @ApiOperation(value = "添加通道信息")
    @PostMapping("/addChannel")
    public BaseResponse addChannel(@RequestBody @ApiParam ChannelDTO channelDTO) {
        return ResultUtil.success(channelService.addChannel(this.getSysUserVO().getUsername(), channelDTO));
    }

    @ApiOperation(value = "修改通道信息")
    @PostMapping("/updateChannel")
    public BaseResponse updateChannel(@RequestBody @ApiParam ChannelDTO channelDTO) {
        return ResultUtil.success(channelService.updateChannel(this.getSysUserVO().getUsername(), channelDTO));
    }

    @ApiOperation(value = "分页查询通道信息")
    @PostMapping("/pageFindChannel")
    public BaseResponse pageFindChannel(@RequestBody @ApiParam ChannelDTO channelDTO) {
        return ResultUtil.success(channelService.pageFindChannel(channelDTO));
    }

    @ApiOperation(value = "根据通道ID查询通道详情")
    @GetMapping("/getChannelById")
    public BaseResponse getChannelById(@RequestParam @ApiParam String channelId) {
        return ResultUtil.success(channelService.getChannelById(channelId));
    }

    @ApiOperation(value = "导出通道信息")
    @PostMapping("/exportChannel")
    public List<ChannelExportVO> exportChannel(@RequestBody @ApiParam ChannelDTO channelDTO) {
        return channelService.exportChannel(channelDTO);
    }

    @ApiOperation(value = "查询所有通道编号")
    @PostMapping("/getAllChannelCode")
    public List<String> getAllChannelCode() {
        return channelService.getAllChannelCode();
    }

    @ApiOperation(value = "代理商渠道查询")
    @PostMapping("/pageAgentChannels")
    public BaseResponse pageAgentChannels(@RequestBody @ApiParam AgentChannelsDTO agentChannelsDTO) {
        agentChannelsDTO.setLanguage(this.getLanguage());
        return ResultUtil.success(channelService.pageAgentChannels(agentChannelsDTO));
    }
}
