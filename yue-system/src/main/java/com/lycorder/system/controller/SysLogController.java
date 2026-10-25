package com.lycorder.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lycorder.core.result.PageResult;
import com.lycorder.system.model.query.LogQuery;
import com.lycorder.system.model.vo.LogPageVO;
import com.lycorder.system.service.SysLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志接口，提供系统操作日志的分页查询能力。
 *
 * @projectName: Real-Time-Api
 * @package: com.lycorder.system.controller
 * @className: SysLogController
 * @author: LYCorder
 * @description: TODO
 * @date: 2026/3/12 23:41
 * @version: 1.0
 */

@Tag(name = "8.日志接口")
@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class SysLogController {

    private final SysLogService sysLogService;

    /**
     * 分页查询系统操作日志。
     *
     * @param queryParams 日志查询参数
     * @return 日志分页数据
     */
    @Operation(summary = "日志分页列表")
    @GetMapping
    @PreAuthorize("@ss.hasPerm('sys:log:list') or @ss.hasRole('ADMIN')")
    public PageResult<LogPageVO> getLogPage(
            LogQuery queryParams
    ) {
        Page<LogPageVO> result = sysLogService.getLogPage(queryParams);
        return PageResult.success(result);
    }
}
