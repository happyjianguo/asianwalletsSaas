package com.asianwallets.permissions.dao;

import com.asianwallets.common.base.BaseMapper;
import com.asianwallets.common.entity.SysRole;
import com.asianwallets.permissions.dto.SysRoleDto;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据角色名,系统ID查询角色
     *
     * @param roleName 角色名
     * @param sysId    系统ID
     * @return 用户实体
     */
    SysRole getSysRoleByNameAndSysId(@Param("roleName") String roleName, @Param("sysId") String sysId);

    /**
     * 查询角色信息
     *
     * @param sysRoleSecDto 角色查询实体
     * @return 角色集合
     */
    List<SysRole> pageGetSysRole(SysRoleDto sysRoleSecDto);

    /**
     * 根据系统ID查询角色ID
     *
     * @param sysId 系统ID
     * @return 角色ID集合
     */
    List<String> selectRoleIdBySysId(String sysId);

    /**
     * 查询默认机构角色ID
     *
     * @return 机构默认角色ID
     */
    SysRole getInstitutionRoleId();

    /**
     * 查询默认POS角色ID
     *
     * @return 机构默认角色ID
     */
    SysRole getPOSRoleId();

    /**
     * 查询默认代理角色ID
     *普通代理商
     *
     * @return 机构默认角色ID
     */
    SysRole getAgencyRoleId();

    /**
     * 渠道代理商
     * @return
     */
    SysRole getAgencyChannelRoleId();

    /**
     * 查询默认商户角色ID
     *
     * @return 机构默认角色ID
     */
    SysRole getMerchantRoleId();

    /**
     * @param sysId    系统ID
     * @param roleCode 角色编码
     * @return 角色ID
     */
    SysRole selectBySysIdAndRoleCode(@Param("sysId") String sysId, @Param("roleCode") String roleCode);
}