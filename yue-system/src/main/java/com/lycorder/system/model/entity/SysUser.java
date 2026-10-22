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
 * 系统用户表
 * @author LYCoder
 * @TableName sys_user
 */

@Schema(description = "系统用户表")
@TableName(value ="sys_user")
@Getter
@Setter
public class SysUser extends BaseEntity {

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    @TableField(value = "username")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 性别((1-男 2-女 0-保密)
     */
    @Schema(description = "性别((1-男 2-女 0-保密)")
    @TableField(value = "gender")
    private Integer gender;

    /**
     * 密码
     */
    @Schema(description = "密码")
    @TableField(value = "password")
    private String password;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    @TableField(value = "dept_id")
    private Long deptId;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 联系方式
     */
    @Schema(description = "联系方式")
    @TableField(value = "mobile")
    private String mobile;

    /**
     * 状态(1-正常 0-禁用)
     */
    @Schema(description = "状态(1-正常 0-禁用)")
    @TableField(value = "status")
    private Integer status;

    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱")
    @TableField(value = "email")
    private String email;


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
     * 逻辑删除标识(0-未删除 1-已删除)
     */
    @Schema(description = "逻辑删除标识(0-未删除 1-已删除)")
    @TableField(value = "is_deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;

    /**
     * 微信 openid
     */
    @Schema(description = "微信 openid")
    @TableField(value = "openid")
    private String openid;
}