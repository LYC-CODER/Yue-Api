package com.lycorder.system.model.query;

import com.lycorder.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知公告查询参数。
 *
 * @author LYCoder
 */
@Schema(description = "通知公告查询参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class NoticeQuery extends BaseQuery {

    /** 通知标题（模糊匹配） */
    @Schema(description = "通知标题")
    private String title;

    /** 发布状态（0-未发布，1-已发布，-1-已撤回） */
    @Schema(description = "发布状态")
    private Integer publishStatus;

    /** 发布时间起（yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss） */
    @Schema(description = "发布时间起")
    private String startTime;

    /** 发布时间止（yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss） */
    @Schema(description = "发布时间止")
    private String endTime;

    /** 查询人 ID（我的通知使用，由服务端填充） */
    @Schema(description = "查询人ID", hidden = true)
    private Long userId;

    /** 是否已读（0-未读，1-已读） */
    @Schema(description = "是否已读")
    private Integer isRead;
}
