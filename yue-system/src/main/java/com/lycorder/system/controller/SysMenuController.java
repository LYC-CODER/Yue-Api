package com.lycorder.system.controller;

import com.lycorder.common.annotation.RepeatSubmit;
import com.lycorder.common.model.Option;
import com.lycorder.core.result.Result;
import com.lycorder.system.model.form.MenuForm;
import com.lycorder.system.model.query.MenuQuery;
import com.lycorder.system.model.vo.MenuVO;
import com.lycorder.system.model.vo.RouteVO;
import com.lycorder.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理接口，负责菜单树查询、表单维护、显示状态调整及当前用户路由生成。
 *
 * @projectName: Real-Time-Api
 * @package: com.lycorder.system.controller
 * @className: SysMenuController
 * @author: LYCorder
 * @description: TODO
 * @date: 2026/3/9 18:08
 * @version: 1.0
 */
@Tag(name = "4.菜单接口")
@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;

    /**
     * 查询菜单树列表。
     *
     * @param queryParams 菜单查询参数
     * @return 菜单树列表
     */
    @Operation(summary = "菜单列表")
    @GetMapping
    @PreAuthorize("@ss.hasPerm('sys:menu:list')")
    public Result<List<MenuVO>> getMenus(MenuQuery queryParams) {
        List<MenuVO> menuList = sysMenuService.listMenus(queryParams);
        return Result.success(menuList);
    }

    /**
     * 获取菜单下拉选项。
     *
     * @param onlyParent 是否只查询父级菜单
     * @return 菜单下拉选项
     */
    @Operation(summary = "菜单下拉列表")
    @GetMapping("/options")
    public Result<List<Option<Long>>> getMenuOptions(
            @Parameter(description = "是否只查询父级菜单")
            @RequestParam(required = false, defaultValue = "false") boolean onlyParent
    ) {
        List<Option<Long>> menus = sysMenuService.listMenuOptions(onlyParent);
        return Result.success(menus);
    }

    /**
     * 获取当前登录用户可访问的菜单路由。
     *
     * @return 菜单路由列表
     */
    @Operation(summary = "当前用户菜单路由列表")
    @GetMapping("/routes")
    public Result<List<RouteVO>> getCurrentUserRoutes() {
        List<RouteVO> routeList = sysMenuService.listCurrentUserRoutes();
        return Result.success(routeList);
    }

    /**
     * 获取菜单编辑表单数据。
     *
     * @param id 菜单ID
     * @return 菜单表单数据
     */
    @Operation(summary = "菜单表单数据")
    @GetMapping("/{id}/form")
    @PreAuthorize("@ss.hasPerm('sys:menu:update')")
    public Result<MenuForm> getMenuForm(
            @Parameter(description = "菜单ID") @PathVariable Long id
    ) {
        MenuForm menu = sysMenuService.getMenuForm(id);
        return Result.success(menu);
    }

    /**
     * 新增菜单。
     *
     * @param menuForm 菜单表单数据
     * @return 操作结果
     */
    @Operation(summary = "新增菜单")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:menu:create')")
    @RepeatSubmit
    public Result<?> addMenu(@RequestBody MenuForm menuForm) {
        boolean result = sysMenuService.saveMenu(menuForm);
        return Result.judge(result);
    }

    /**
     * 修改菜单信息。
     *
     * @param menuForm 菜单表单数据
     * @return 操作结果
     */
    @Operation(summary = "修改菜单")
    @PutMapping(value = "/{id}")
    @PreAuthorize("@ss.hasPerm('sys:menu:update')")
    public Result<?> updateMenu(
            @RequestBody MenuForm menuForm
    ) {
        boolean result = sysMenuService.saveMenu(menuForm);
        return Result.judge(result);
    }

    /**
     * 删除指定菜单及其子菜单。
     *
     * @param id 菜单ID
     * @return 操作结果
     */
    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('sys:menu:delete')")
    public Result<?> deleteMenu(
            @Parameter(description = "菜单ID，多个以英文(,)分割") @PathVariable("id") Long id
    ) {
        boolean result = sysMenuService.deleteMenu(id);
        return Result.judge(result);
    }

    /**
     * 修改菜单显示状态。
     *
     * @param menuId  菜单ID
     * @param visible 显示状态（1：显示；0：隐藏）
     * @return 操作结果
     */
    @Operation(summary = "修改菜单显示状态")
    @PatchMapping("/{menuId}")
    @PreAuthorize("@ss.hasPerm('sys:menu:update')")
    public Result<?> updateMenuVisible(
            @Parameter(description = "菜单ID") @PathVariable Long menuId,
            @Parameter(description = "显示状态(1:显示;0:隐藏)") Integer visible
    ) {
        boolean result = sysMenuService.updateMenuVisible(menuId, visible);
        return Result.judge(result);
    }

}
