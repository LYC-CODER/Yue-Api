package com.lycorder.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lycorder.common.annotation.Log;
import com.lycorder.common.annotation.RepeatSubmit;
import com.lycorder.common.enums.LogModuleEnum;
import com.lycorder.core.result.PageResult;
import com.lycorder.core.result.Result;
import com.lycorder.system.model.form.NoticeForm;
import com.lycorder.system.model.query.NoticeQuery;
import com.lycorder.system.model.vo.NoticeDetailVO;
import com.lycorder.system.model.vo.NoticePageVO;
import com.lycorder.system.model.vo.UserNoticePageVO;
import com.lycorder.system.service.SysNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 通知公告接口。
 *
 * @author LYCoder
 */
@Tag(name = "9.通知公告")
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class SysNoticeController {

    private final SysNoticeService sysNoticeService;

    /**
     * 管理端通知分页。
     *
     * @param queryParams 查询参数
     * @return 通知分页数据
     */
    @Operation(summary = "通知公告分页列表")
    @GetMapping
    @PreAuthorize("@ss.hasPerm('sys:notice:list')")
    public PageResult<NoticePageVO> getNoticePage(NoticeQuery queryParams) {
        Page<NoticePageVO> result = sysNoticeService.getNoticePage(queryParams);
        return PageResult.success(result);
    }

    /**
     * 获取通知编辑表单。
     *
     * @param id 通知 ID
     * @return 通知表单
     */
    @Operation(summary = "获取通知公告表单")
    @GetMapping("/{id}/form")
    @PreAuthorize("@ss.hasPerm('sys:notice:update')")
    public Result<NoticeForm> getNoticeForm(@PathVariable Long id) {
        return Result.success(sysNoticeService.getNoticeFormData(id));
    }

    /**
     * 新增通知公告。
     *
     * @param formData 通知表单
     * @return 操作结果
     */
    @Operation(summary = "新增通知公告")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:notice:create')")
    @RepeatSubmit
    @Log(value = "新增通知公告", module = LogModuleEnum.OTHER)
    public Result<Void> saveNotice(@RequestBody @Valid NoticeForm formData) {
        return Result.judge(sysNoticeService.saveNotice(formData));
    }

    /**
     * 修改通知公告。
     *
     * @param id       通知 ID
     * @param formData 通知表单
     * @return 操作结果
     */
    @Operation(summary = "修改通知公告")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('sys:notice:update')")
    @RepeatSubmit
    @Log(value = "修改通知公告", module = LogModuleEnum.OTHER)
    public Result<Void> updateNotice(@PathVariable Long id,
                                     @RequestBody @Valid NoticeForm formData) {
        return Result.judge(sysNoticeService.updateNotice(id, formData));
    }

    /**
     * 管理端查看通知详情。
     *
     * @param id 通知 ID
     * @return 通知详情
     */
    @Operation(summary = "通知公告详情")
    @GetMapping("/{id}/detail")
    @PreAuthorize("@ss.hasPerm('sys:notice:list')")
    public Result<NoticeDetailVO> getNoticeDetail(@PathVariable Long id) {
        return Result.success(sysNoticeService.getNoticeDetail(id));
    }

    /**
     * 发布通知公告。
     *
     * @param id 通知 ID
     * @return 操作结果
     */
    @Operation(summary = "发布通知公告")
    @PutMapping("/{id}/publish")
    @PreAuthorize("@ss.hasPerm('sys:notice:publish')")
    @RepeatSubmit
    @Log(value = "发布通知公告", module = LogModuleEnum.OTHER)
    public Result<Void> publishNotice(@PathVariable Long id) {
        return Result.judge(sysNoticeService.publishNotice(id));
    }

    /**
     * 撤回通知公告。
     *
     * @param id 通知 ID
     * @return 操作结果
     */
    @Operation(summary = "撤回通知公告")
    @PutMapping("/{id}/revoke")
    @PreAuthorize("@ss.hasPerm('sys:notice:revoke')")
    @RepeatSubmit
    @Log(value = "撤回通知公告", module = LogModuleEnum.OTHER)
    public Result<Void> revokeNotice(@PathVariable Long id) {
        return Result.judge(sysNoticeService.revokeNotice(id));
    }

    /**
     * 删除通知公告。
     *
     * @param ids 通知 ID，多个用英文逗号分隔
     * @return 操作结果
     */
    @Operation(summary = "删除通知公告")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:notice:delete')")
    @Log(value = "删除通知公告", module = LogModuleEnum.OTHER)
    public Result<Void> deleteNotices(@PathVariable String ids) {
        return Result.judge(sysNoticeService.deleteNotices(ids));
    }

    /**
     * 当前用户通知分页。
     *
     * @param queryParams 查询参数
     * @return 我的通知分页
     */
    @Operation(summary = "我的通知分页列表")
    @GetMapping("/my")
    public PageResult<UserNoticePageVO> getMyNoticePage(NoticeQuery queryParams) {
        Page<UserNoticePageVO> result = sysNoticeService.getMyNoticePage(queryParams);
        return PageResult.success(result);
    }

    /**
     * 阅读我的通知详情，并标记已读。
     *
     * @param id 通知 ID
     * @return 通知详情
     */
    @Operation(summary = "阅读我的通知详情")
    @GetMapping("/my/{id}")
    public Result<NoticeDetailVO> getMyNoticeDetail(@PathVariable Long id) {
        return Result.success(sysNoticeService.getMyNoticeDetail(id));
    }

    /**
     * 全部标记已读。
     *
     * @return 操作结果
     */
    @Operation(summary = "全部已读")
    @PutMapping("/read-all")
    public Result<Void> readAll() {
        sysNoticeService.readAll();
        return Result.success();
    }

    /**
     * 当前用户未读数量。
     *
     * @return 未读数量
     */
    @Operation(summary = "未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(sysNoticeService.getUnreadCount());
    }
}
