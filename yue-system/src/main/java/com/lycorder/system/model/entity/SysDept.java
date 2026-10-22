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
 * 部门管理表
 * @author LYCorder
 * @TableName sys_dept
 */

@Schema(description = "部门管理表")
@TableName(value ="sys_dept")
@Getter
@Setter
public class SysDept extends BaseEntity {


    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    @TableField(value = "name")
    private String name;

    /**
     * 部门编号
     */
    @Schema(description = "部门编号")
    @TableField(value = "code")
    private String code;

    /**
     * 父节点id
     */
    @Schema(description = "父节点id")
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 父节点id路径
     */
    @Schema(description = "父节点id路径")
    @TableField(value = "tree_path")
    private String treePath;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序")
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 状态(1-正常 0-禁用)
     */
    @Schema(description = "状态(1-正常 0-禁用)")
    @TableField(value = "status")
    private Integer status;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    @TableField(value = "create_by")
    private Long createBy;

    /**
     * 修改人ID
     */
    @Schema(description = "修改人ID")
    @TableField(value = "update_by")
    private Long updateBy;

    /**
     * 逻辑删除标识(1-已删除 0-未删除)
     */
    @Schema(description = "逻辑删除标识(1-已删除 0-未删除)")
    @TableField(value = "is_deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}