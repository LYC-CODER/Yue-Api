package com.lycorder.system.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知公告详情。
 *
 * @author LYCoder
 */
@Data
@Schema(description = "通知公告详情")
public class NoticeDetailVO {

    /** 通知 ID */
    @Schema(description = "通知ID")
    private Long id;

    /** 通知标题 */
    @Schema(description = "通知标题")
    private String title;

    /** 通知内容 */
    @Schema(description = "通知内容")
    private String content;

    /** 通知类型 */
    @Schema(description = "通知类型")
    private Integer type;

    /** 通知等级 */
    @Schema(description = "通知等级")
    private String level;

    /** 发布人 */
    @Schema(description = "发布人")
    private String publisherName;

    /** 发布状态 */
    @Schema(description = "发布状态")
    private Integer publishStatus;

    /** 发布时间 */
    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}
