package com.lycorder.system.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知公告分页项。
 *
 * @author LYCoder
 */
@Data
@Schema(description = "通知公告分页项")
public class NoticePageVO {

    /** 通知 ID */
    @Schema(description = "通知ID")
    private Long id;

    /** 通知标题 */
    @Schema(description = "通知标题")
    private String title;

    /** 通知类型 */
    @Schema(description = "通知类型")
    private Integer type;

    /** 通知等级 */
    @Schema(description = "通知等级")
    private String level;

    /** 目标类型（1-全体，2-指定用户） */
    @Schema(description = "目标类型")
    private Integer targetType;

    /** 发布状态（0-未发布，1-已发布，-1-已撤回） */
    @Schema(description = "发布状态")
    private Integer publishStatus;

    /** 发布人昵称 */
    @Schema(description = "发布人")
    private String publisherName;

    /** 发布时间 */
    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    /** 撤回时间 */
    @Schema(description = "撤回时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime revokeTime;

    /** 创建时间 */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
