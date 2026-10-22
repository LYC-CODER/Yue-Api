package com.lycorder.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户角色关联表
 * @author LYCorder
 * @TableName sys_user_role
 */

@Schema(description = "用户角色关联表")
@TableName(value ="sys_user_role")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysUserRole {
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    @TableId(value = "user_id")
    private Long userId;

    /**
     * 角色ID
     */
    @Schema(description = "角色ID")
    @TableField(value = "role_id")
    private Long roleId;

}