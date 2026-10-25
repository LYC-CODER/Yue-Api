package com.lycorder.system.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 我的通知分页项。
 *
 * @author LYCoder
 */
@Data
@Schema(description = "我的通知分页项")
public class UserNoticePageVO {

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

    /** 发布人 */
    @Schema(description = "发布人")
    private String publisherName;

    /** 发布时间 */
    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    /** 是否已读（0-未读，1-已读） */
    @Schema(description = "是否已读")
    private Integer isRead;

    /** 阅读时间 */
    @Schema(description = "阅读时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;
}
