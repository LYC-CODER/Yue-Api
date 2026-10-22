package com.lycorder.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色菜单关联表
 * @author LYCorder
 * @TableName sys_role_menu
 */

@Schema(description = "角色菜单关联表")
@TableName(value ="sys_role_menu")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysRoleMenu {
    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    @TableField(value = "role_id")
    private Long roleId;

    /**
     * 菜单ID
     */
    @Schema(description = "菜单ID")
    @TableField(value = "menu_id")
    private Long menuId;
}