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

    @Operation(summary = "角色分页列表")
    @GetMapping
    public PageResult<RolePageVO> getRolePage(
            RoleQuery queryParams
    ) {
        Page<RolePageVO> result = sysRoleService.getRolePage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "角色下拉列表")
    @GetMapping("/options")
    public Result<List<Option<Long>>> listRoleOptions() {
        List<Option<Long>> list = sysRoleService.listRoleOptions();
        return Result.success(list);
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:role:create')")
    @RepeatSubmit
    public Result<?> addRole(@Valid @RequestBody RoleForm roleForm) {
        boolean result = sysRoleService.saveRole(roleForm);
        return Result.judge(result);
    }


    @Operation(summary = "获取角色表单数据")
    @GetMapping("/{roleId}/form")
    @PreAuthorize("@ss.hasPerm('sys:role:update')")
    public Result<RoleForm> getRoleForm(
            @Parameter(description = "角色ID") @PathVariable Long roleId
    ) {
        RoleForm roleForm = sysRoleService.getRoleForm(roleId);
        return Result.success(roleForm);
    }


    @Operation(summary = "修改角色")
    @PutMapping(value = "/{id}")
    @PreAuthorize("@ss.hasPerm('sys:role:update')")
    public Result<?> updateRole(@Valid @RequestBody RoleForm roleForm) {
        boolean result = sysRoleService.saveRole(roleForm);
        return Result.judge(result);
    }


    @Operation(summary = "删除角色")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:role:delete')")
    public Result<Void> deleteRoles(
            @Parameter(description = "删除角色，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        sysRoleService.deleteRoles(ids);
        return Result.success();
    }

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


    @Operation(summary = "获取角色的菜单ID集合")
    @GetMapping("/{roleId}/menuIds")
    public Result<List<Long>> getRoleMenuIds(
            @Parameter(description = "角色ID") @PathVariable Long roleId
    ) {
        List<Long> menuIds = sysRoleService.getRoleMenuIds(roleId);
        return Result.success(menuIds);
    }


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
