package com.asianwallets.permissions.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel(value = "用户角色关联输入实体", description = "用户角色关联输入实体")
public class SysUserRoleDto {

    @ApiModelProperty(value = "用户账户")
    private String userId;

    @ApiModelProperty(value = "用户账户")
    private String username;

    @ApiModelProperty(value = "登录密码")
    private String password;

    @ApiModelProperty(value = "交易密码")
    private String tradePassword;

    @ApiModelProperty(value = "用户名称")
    private String name;

    @ApiModelProperty(value = "权限类型")
    private Integer permissionType;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "系统Id")
    private String sysId;

    @ApiModelProperty(value = "启用禁用")
    private Boolean enabled;

    @ApiModelProperty(value = "角色Id")
    private List<String> roleIdList;

    @ApiModelProperty(value = "权限Id")
    private List<String> menuIdList;
}
