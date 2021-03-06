package com.asianwallets.permissions.feign.base;

import com.asianwallets.common.dto.InstitutionChannelQueryDTO;
import com.asianwallets.common.dto.InstitutionProductChannelDTO;
import com.asianwallets.common.dto.InstitutionProductDTO;
import com.asianwallets.common.dto.InstitutionRequestDTO;
import com.asianwallets.common.response.BaseResponse;
import com.asianwallets.permissions.feign.base.impl.InstitutionProductChannelFeignImpl;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "asianwallets-base", fallback = InstitutionProductChannelFeignImpl.class)
public interface InstitutionProductChannelFeign {

    @ApiOperation(value = "新增机构关联产品通道信息")
    @PostMapping("/insProCha/addInsProCha")
    BaseResponse addInsProCha(@RequestBody @ApiParam List<InstitutionProductChannelDTO> institutionProductChannelDTOList);

    @ApiOperation(value = "修改机构关联产品通道信息")
    @PostMapping("/insProCha/updateInsProCha")
    BaseResponse updateInsProCha(@RequestBody @ApiParam List<InstitutionProductChannelDTO> institutionProductChannelDTOList);

    @ApiOperation(value = "根据机构ID查询机构关联产品通道信息")
    @GetMapping("/insProCha/getInsProChaByInsId")
    BaseResponse getInsProChaByInsId(@RequestParam("insId") @ApiParam String insId, @RequestParam("merId") @ApiParam String merId);

    @ApiOperation(value = "查询所有产品关联通道信息(前端用)")
    @PostMapping("/insProCha/getAllProCha")
    BaseResponse getAllProCha();

    @ApiOperation(value = "分页查询机构参数设置")
    @PostMapping("/insProCha/pageInstitutionRequests")
    BaseResponse pageInstitutionRequests(@RequestBody @ApiParam InstitutionRequestDTO institutionRequestDTO);

    @ApiOperation(value = "分页查询机构产品信息")
    @PostMapping("/insProCha/pageInstitutionPro")
    BaseResponse pageInstitutionPro(@RequestBody @ApiParam InstitutionProductDTO institutionProductDTO);

    @ApiOperation(value = "分页查询机构通道信息")
    @PostMapping("/insProCha/pageInstitutionCha")
    BaseResponse pageInstitutionCha(@RequestBody @ApiParam InstitutionChannelQueryDTO institutionChannelQueryDTO);

}
