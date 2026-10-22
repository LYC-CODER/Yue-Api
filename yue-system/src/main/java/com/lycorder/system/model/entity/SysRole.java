package com.lycorder.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lycorder.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统角色表
 * @author LYCorder
 * @TableName sys_role
 */

@Schema(description = "系统角色表")
@TableName(value ="sys_role")
@Getter
@Setter
public class SysRole extends BaseEntity {

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    @TableField(value = "name")
    private String name;

    /**
     * 角色编码
     */
    @Schema(description = "角色编码")
    @TableField(value = "code")
    private String code;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序")
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 角色状态(1-正常 0-停用)
     */
    @Schema(description = "角色状态(1-正常 0-停用)")
    @TableField(value = "status")
    private Integer status;

    /**
     * 数据权限(1-所有数据 2-部门及子部门数据 3-本部门数据 4-本人数据)
     */
    @Schema(description = "数据权限(1-所有数据 2-部门及子部门数据 3-本部门数据 4-本人数据)")
    @TableField(value = "data_scope")
    private Integer dataScope;

    /**
     * 创建人 ID
     */
    @Schema(description = "创建人 ID")
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID")
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 逻辑删除标识(0-未删除 1-已删除)
     */
    @Schema(description = "逻辑删除标识(0-未删除 1-已删除)")
    @TableField(value = "is_deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}