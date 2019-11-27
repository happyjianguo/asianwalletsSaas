package com.asianwallets.permissions.service;

import com.asianwallets.common.dto.InstitutionDTO;
import com.asianwallets.common.entity.SysRole;
import com.asianwallets.common.vo.SysUserVO;
import com.asianwallets.permissions.dto.*;
import com.asianwallets.permissions.vo.SysUserDetailVO;
import com.asianwallets.permissions.vo.SysUserSecVO;
import com.github.pagehelper.PageInfo;

/**
 * 用户业务接口
 */
public interface SysUserService {

    /**
     * 根据用户名查询用户关联角色,权限信息
     *
     * @param userName 用户名
     * @return SysUserVO
     */
    SysUserVO getSysUser(String userName);

    /**
     * 运营后台新增用户角色,用户权限信息
     *
     * @param username       用户名
     * @param sysUserRoleDto 用户角色输入实体
     * @return 修改条数
     */
    int addSysUserByOperation(String username, SysUserRoleDto sysUserRoleDto);

    /**
     * 运营后台修改用户角色,用户权限信息
     *
     * @param username       用户名
     * @param sysUserRoleDto 用户角色输入实体
     * @return 修改条数
     */
    int updateSysUserByOperation(String username, SysUserRoleDto sysUserRoleDto);

    /**
     * 新增角色权限信息
     *
     * @param username       用户名
     * @param sysRoleMenuDto 角色权限输入实体
     * @return 修改条数
     */
    int addSysRole(String username, SysRoleMenuDto sysRoleMenuDto);

    /**
     * 修改角色权限信息
     *
     * @param username       用户名
     * @param sysRoleMenuDto 角色权限输入实体
     * @return 修改条数
     */
    int updateSysRole(String username, SysRoleMenuDto sysRoleMenuDto);

    /**
     * 分页查询用户信息
     *
     * @param sysUserSecDto 角色权限输入实体
     * @return 修改条数
     */
    PageInfo<SysUserSecVO> pageGetSysUser(SysUserDto sysUserSecDto);

    /**
     * 分页查询角色信息
     *
     * @param sysRoleSecDto 角色权限输入实体
     * @return 修改条数
     */
    PageInfo<SysRole> pageGetSysRole(SysRoleDto sysRoleSecDto);

    /**
     * 重置登录密码
     *
     * @param username 用户名
     * @param userId   用户ID
     * @return 修改条数
     */
    int resetPassword(String username, String userId);

    /**
     * 修改登录密码
     *
     * @param username          用户名
     * @param updatePasswordDto 修改密码实体
     * @return 修改条数
     */
    int updatePassword(String username, UpdatePasswordDto updatePasswordDto);

    /**
     * 修改交易密码
     *
     * @param username          用户名
     * @param updatePasswordDto 修改密码实体
     * @return 修改条数
     */
    int updateTradePassword(String username, UpdatePasswordDto updatePasswordDto);

    /**
     * 查询用户详情
     *
     * @param username 用户名
     * @return 用户详情实体
     */
    SysUserDetailVO getSysUserDetail(String username);

    /**
     * 机构开户后发送邮件
     * @param institutionDTO
     */
    void openAccountEmail(InstitutionDTO institutionDTO);
}
