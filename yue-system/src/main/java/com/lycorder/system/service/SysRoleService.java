package com.lycorder.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lycorder.common.model.Option;
import com.lycorder.system.model.entity.SysRole;
import com.lycorder.system.model.form.RoleForm;
import com.lycorder.system.model.query.RoleQuery;
import com.lycorder.system.model.vo.RolePageVO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Set;


/**
* @author LYCorder
* @description 针对表【sys_role(系统角色表)】的数据库操作Service
* @createDate 2026-01-23 19:26:46
*/
public interface SysRoleService extends IService<SysRole> {

    /**
     * 获取用户角色数据权限范围
     *
     * @param roles 角色ID集合
     * @return 最大数据权限范围
     */
    Integer getMaximumDataScope(Set<String> roles);

    /**
     * 角色分页列表
     *
     * @param queryParams 查询参数
     * @return 角色分页列表
     */
    Page<RolePageVO> getRolePage(RoleQuery queryParams);

    /**
     * 角色下拉列表
     *
     * @return 角色下拉列表
     */
    List<Option<Long>> listRoleOptions();

    /**
     * 新增角色
     *
     * @param roleForm 角色表单
     * @return 是否成功
     */
    boolean saveRole(@Valid RoleForm roleForm);


    /**
     * 获取角色表单数据
     *
     * @param roleId 角色ID
     * @return  {@link RoleForm} – 角色表单数据
     */
    RoleForm getRoleForm(Long roleId);

    /**
     * 删除角色
     *
     * @param ids 角色ID，多个以英文逗号(,)分割
     */
    void deleteRoles(String ids);

    /**
     * 修改角色状态
     *
     * @param roleId 角色ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateRoleStatus(Long roleId, Integer status);

    /**
     * 获取角色菜单ID集合
     *
     * @param roleId 角色ID
     * @return 角色菜单ID集合
     */
    List<Long> getRoleMenuIds(Long roleId);

    /**
     * 分配菜单权限
     *
     * @param roleId 角色ID
     * @param menuIds 菜单ID集合
     */
    void assignMenusToRole(Long roleId, List<Long> menuIds);
}
