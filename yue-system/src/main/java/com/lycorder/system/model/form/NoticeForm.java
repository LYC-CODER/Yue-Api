package com.lycorder.system.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.util.List;

/**
 * 通知公告表单。
 *
 * @author LYCoder
 */
@Schema(description = "通知公告表单")
@Data
public class NoticeForm {

    /** 通知 ID（编辑时传入） */
    @Schema(description = "通知ID")
    private Long id;

    /** 通知标题 */
    @Schema(description = "通知标题")
    @NotBlank(message = "通知标题不能为空")
    @Size(max = 50, message = "通知标题长度不能超过50个字符")
    private String title;

    /** 通知内容 */
    @Schema(description = "通知内容")
    @NotBlank(message = "通知内容不能为空")
    @Size(max = 20000, message = "通知内容长度不能超过20000个字符")
    private String content;

    /** 通知类型（字典 notice_type） */
    @Schema(description = "通知类型")
    @NotNull(message = "通知类型不能为空")
    private Integer type;

    /** 通知等级（字典 notice_level） */
    @Schema(description = "通知等级")
    @NotBlank(message = "通知等级不能为空")
    @Size(max = 5, message = "通知等级长度不能超过5个字符")
    private String level;

    /** 目标类型（1-全体，2-指定用户） */
    @Schema(description = "目标类型（1-全体，2-指定用户）")
    @NotNull(message = "目标类型不能为空")
    @Range(min = 1, max = 2, message = "目标类型取值范围[1,2]")
    private Integer targetType;

    /** 指定用户 ID 集合 */
    @Schema(description = "指定用户ID集合")
    private List<Long> targetUserIds;
}
