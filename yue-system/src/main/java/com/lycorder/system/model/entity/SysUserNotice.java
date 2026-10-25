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
 * 用户通知状态实体。
 *
 * <p>对应 sys_user_notice 表，记录公告的接收人、已读状态和阅读时间。</p>
 *
 * @author LYCoder
 */
@Getter
@Setter
@Schema(description = "用户通知状态")
@TableName("sys_user_notice")
public class SysUserNotice extends BaseEntity {

    /** 通知公告 ID */
    @Schema(description = "通知公告ID")
    @TableField("notice_id")
    private Long noticeId;

    /** 接收用户 ID */
    @Schema(description = "接收用户ID")
    @TableField("user_id")
    private Long userId;

    /** 读取状态（0-未读，1-已读） */
    @Schema(description = "读取状态（0-未读，1-已读）")
    @TableField("is_read")
    private Integer isRead;

    /** 阅读时间 */
    @Schema(description = "阅读时间")
    @TableField("read_time")
    private LocalDateTime readTime;

    /** 逻辑删除标识（0-未删除，1-已删除） */
    @Schema(description = "逻辑删除标识")
    @TableField("is_deleted")
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;
}
