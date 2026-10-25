package com.lycorder.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lycorder.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 通知公告实体。
 *
 * <p>对应 sys_notice 表，保存公告内容、目标范围和发布状态。</p>
 *
 * @author LYCoder
 */
@Getter
@Setter
@Schema(description = "通知公告")
@TableName("sys_notice")
public class SysNotice extends BaseEntity {

    /** 通知标题 */
    @Schema(description = "通知标题")
    @TableField("title")
    private String title;

    /** 通知内容 */
    @Schema(description = "通知内容")
    @TableField("content")
    private String content;

    /** 通知类型（字典 notice_type） */
    @Schema(description = "通知类型")
    @TableField("type")
    private Integer type;

    /** 通知等级（字典 notice_level） */
    @Schema(description = "通知等级")
    @TableField("level")
    private String level;

    /** 目标类型（1-全体，2-指定用户） */
    @Schema(description = "目标类型（1-全体，2-指定用户）")
    @TableField("target_type")
    private Integer targetType;

    /** 目标用户 ID，多个使用英文逗号分隔 */
    @Schema(description = "目标用户ID集合")
    @TableField("target_user_ids")
    private String targetUserIds;

    /** 发布人 ID */
    @Schema(description = "发布人ID")
    @TableField("publisher_id")
    private Long publisherId;

    /** 发布状态（0-未发布，1-已发布，-1-已撤回） */
    @Schema(description = "发布状态（0-未发布，1-已发布，-1-已撤回）")
    @TableField("publish_status")
    private Integer publishStatus;

    /** 发布时间 */
    @Schema(description = "发布时间")
    @TableField("publish_time")
    private LocalDateTime publishTime;

    /** 撤回时间 */
    @Schema(description = "撤回时间")
    @TableField("revoke_time")
    private LocalDateTime revokeTime;

    /** 创建人 ID */
    @Schema(description = "创建人ID")
    @TableField("create_by")
    private Long createBy;

    /** 更新人 ID */
    @Schema(description = "更新人ID")
    @TableField("update_by")
    private Long updateBy;

    /** 逻辑删除标识（0-未删除，1-已删除） */
    @Schema(description = "逻辑删除标识")
    @TableField("is_deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
