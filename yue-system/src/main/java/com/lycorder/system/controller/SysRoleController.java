package com.lycorder.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lycorder.common.annotation.RepeatSubmit;
import com.lycorder.common.model.Option;
import com.lycorder.core.result.PageResult;
import com.lycorder.core.result.Result;
import com.lycorder.system.model.form.RoleForm;
import com.lycorder.system.model.query.RoleQuery;
import com.lycorder.system.model.vo.RolePageVO;
import com.lycorder.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理接口，负责角色分页查询、维护、状态调整及菜单权限分配。
 *
 * @projectName: Real-Time-Api
 * @package: com.lycorder.system.controller
 * @className: SysRoleController
 * @author: LYCorder
 * @description: TODO
 * @date: 2026/3/11 11:57
 * @version: 1.0
 */

@Tag(name = "3.角色接口")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class SysRoleController {
    private final SysRoleService sysRoleService;

    /**
     * 分页查询角色列表。
     *
     * @param queryParams 角色查询参数
     * @return 角色分页数据
     */
    @Operation(summary = "角色分页列表")
    @GetMapping
    @PreAuthorize("@ss.hasPerm('sys:role:list')")
    public PageResult<RolePageVO> getRolePage(
            RoleQuery queryParams
    ) {
        Page<RolePageVO> result = sysRoleService.getRolePage(queryParams);
        return PageResult.success(result);
    }

    /**
     * 获取角色下拉选项。
     *
     * @return 角色选项列表
     */
    @Operation(summary = "角色下拉列表")
    @GetMapping("/options")
    public Result<List<Option<Long>>> listRoleOptions() {
        List<Option<Long>> list = sysRoleService.listRoleOptions();
        return Result.success(list);
    }

    /**
     * 新增角色。
     *
     * @param roleForm 角色表单数据
     * @return 操作结果
     */
    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:role:create')")
    @RepeatSubmit
    public Result<?> addRole(@Valid @RequestBody RoleForm roleForm) {
        boolean result = sysRoleService.saveRole(roleForm);
        return Result.judge(result);
    }


    /**
     * 获取角色编辑表单数据。
     *
     * @param roleId 角色ID
     * @return 角色表单数据
     */
    @Operation(summary = "获取角色表单数据")
    @GetMapping("/{roleId}/form")
    @PreAuthorize("@ss.hasPerm('sys:role:update')")
    public Result<RoleForm> getRoleForm(
            @Parameter(description = "角色ID") @PathVariable Long roleId
    ) {
        RoleForm roleForm = sysRoleService.getRoleForm(roleId);
        return Result.success(roleForm);
    }


    /**
     * 修改角色信息，角色ID由表单携带。
     *
     * @param roleForm 角色表单数据
     * @return 操作结果
     */
    @Operation(summary = "修改角色")
    @PutMapping(value = "/{id}")
    @PreAuthorize("@ss.hasPerm('sys:role:update')")
    public Result<?> updateRole(@Valid @RequestBody RoleForm roleForm) {
        boolean result = sysRoleService.saveRole(roleForm);
        return Result.judge(result);
    }


    /**
     * 批量删除角色。
     *
     * @param ids 角色ID字符串，多个以英文逗号分隔
     * @return 操作结果
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:role:delete')")
    public Result<Void> deleteRoles(
            @Parameter(description = "删除角色，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        sysRoleService.deleteRoles(ids);
        return Result.success();
    }

    /**
     * 修改角色启用状态。
     *
     * @param roleId 角色ID
     * @param status 状态（1：启用；0：禁用）
     * @return 操作结果
     */
    @Operation(summary = "修改角色状态")
    @PutMapping(value = "/{roleId}/status")
    @PreAuthorize("@ss.hasPerm('sys:role:update')")
    public Result<?> updateRoleStatus(
            @Parameter(description = "角色ID") @PathVariable Long roleId,
            @Parameter(description = "状态(1:启用;0:禁用)") @RequestParam Integer status
    ) {
        boolean result = sysRoleService.updateRoleStatus(roleId, status);
        return Result.judge(result);
    }


    /**
     * 获取角色已分配的菜单ID集合。
     *
     * @param roleId 角色ID
     * @return 菜单ID集合
     */
    @Operation(summary = "获取角色的菜单ID集合")
    @GetMapping("/{roleId}/menuIds")
    @PreAuthorize("@ss.hasPerm('sys:role:list')")
    public Result<List<Long>> getRoleMenuIds(
            @Parameter(description = "角色ID") @PathVariable Long roleId
    ) {
        List<Long> menuIds = sysRoleService.getRoleMenuIds(roleId);
        return Result.success(menuIds);
    }


    /**
     * 为角色分配菜单权限。
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID集合
     * @return 操作结果
     */
    @Operation(summary = "角色分配菜单权限")
    @PutMapping("/{roleId}/menus")
    @PreAuthorize("@ss.hasPerm('sys:role:assign')")
    public Result<Void> assignMenusToRole(
            @PathVariable Long roleId,
            @RequestBody List<Long> menuIds
    ) {
        sysRoleService.assignMenusToRole(roleId, menuIds);
        return Result.success();
    }
}
