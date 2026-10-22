package com.lycorder.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lycorder.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统菜单表
 * @author LYCorder
 * @TableName sys_menu
 */

@Schema(description = "系统菜单表")
@TableName(value ="sys_menu")
@Getter
@Setter
public class SysMenu extends BaseEntity {

    /**
     * 父菜单ID
     */
    @Schema(description = "父菜单ID")
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 父节点ID路径
     */
    @Schema(description = "父节点ID路径")
    @TableField(value = "tree_path")
    private String treePath;

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称")
    @TableField(value = "name")
    private String name;

    /**
     * 菜单类型（C-目录 M-菜单 B-按钮）
     */
    @Schema(description = "菜单类型（C-目录 M-菜单 B-按钮）")
    @TableField(value = "type")
    private String type;

    /**
     * 路由名称（Vue Router 中用于命名路由）
     */
    @Schema(description = "路由名称（Vue Router 中用于命名路由）")
    @TableField(value = "route_name")
    private String routeName;

    /**
     * 路由路径（Vue Router 中定义的 URL 路径）
     */
    @Schema(description = "路由路径（Vue Router 中定义的 URL 路径）")
    @TableField(value = "route_path")
    private String routePath;

    /**
     * 组件路径（组件页面完整路径，相对于 src/views/，缺省后缀 .vue）
     */
    @Schema(description = "组件路径（组件页面完整路径，相对于 src/views/，缺省后缀 .vue）")
    @TableField(value = "component")
    private String component;

    /**
     * 【按钮】权限标识
     */
    @Schema(description = "【按钮】权限标识")
    @TableField(value = "perm")
    private String perm;

    /**
     * 【目录】只有一个子路由是否始终显示（1-是 0-否）
     */
    @Schema(description = "【目录】只有一个子路由是否始终显示（1-是 0-否）")
    @TableField(value = "always_show")
    private Integer alwaysShow;

    /**
     * 【菜单】是否开启页面缓存（1-是 0-否）
     */
    @Schema(description = "【菜单】是否开启页面缓存（1-是 0-否）")
    @TableField(value = "keep_alive")
    private Integer keepAlive;

    /**
     * 显示状态（1-显示 0-隐藏）
     */
    @Schema(description = "显示状态（1-显示 0-隐藏）")
    @TableField(value = "visible")
    private Integer visible;

    /**
     * 排序
     */
    @Schema(description = "排序")
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 菜单图标
     */
    @Schema(description = "菜单图标")
    @TableField(value = "icon")
    private String icon;

    /**
     * 跳转路径
     */
    @Schema(description = "跳转路径")
    @TableField(value = "redirect")
    private String redirect;


    /**
     * 路由参数
     */
    @Schema(description = "路由参数")
    @TableField(value = "params")
    private String params;
}