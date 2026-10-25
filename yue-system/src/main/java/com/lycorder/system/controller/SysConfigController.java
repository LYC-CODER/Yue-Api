package com.lycorder.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lycorder.common.annotation.Log;
import com.lycorder.common.annotation.RepeatSubmit;
import com.lycorder.common.enums.LogModuleEnum;
import com.lycorder.core.result.PageResult;
import com.lycorder.core.result.Result;
import com.lycorder.system.model.form.ConfigForm;
import com.lycorder.system.model.query.ConfigQuery;
import com.lycorder.system.model.vo.ConfigPageVO;
import com.lycorder.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置接口。
 *
 * @author LYCoder
 */
@Tag(name = "10.系统配置")
@RestController
@RequestMapping("/api/v1/configs")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigService sysConfigService;

    /**
     * 配置分页列表。
     *
     * @param queryParams 查询参数
     * @return 配置分页数据
     */
    @Operation(summary = "系统配置分页列表")
    @GetMapping
    @PreAuthorize("@ss.hasPerm('sys:config:list')")
    @Log(value = "系统配置分页列表", module = LogModuleEnum.SETTING)
    public PageResult<ConfigPageVO> getConfigPage(ConfigQuery queryParams) {
        Page<ConfigPageVO> result = sysConfigService.getConfigPage(queryParams);
        return PageResult.success(result);
    }

    /**
     * 获取配置编辑表单。
     *
     * @param id 配置 ID
     * @return 配置表单
     */
    @Operation(summary = "获取系统配置表单")
    @GetMapping("/{id}/form")
    @PreAuthorize("@ss.hasPerm('sys:config:update')")
    public Result<ConfigForm> getConfigForm(@PathVariable Long id) {
        return Result.success(sysConfigService.getConfigForm(id));
    }

    /**
     * 新增配置。
     *
     * @param formData 配置表单
     * @return 操作结果
     */
    @Operation(summary = "新增系统配置")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:config:create')")
    @RepeatSubmit
    @Log(value = "新增系统配置", module = LogModuleEnum.SETTING)
    public Result<Void> saveConfig(@RequestBody @Valid ConfigForm formData) {
        return Result.judge(sysConfigService.saveConfig(formData));
    }

    /**
     * 修改配置。
     *
     * @param id       配置 ID
     * @param formData 配置表单
     * @return 操作结果
     */
    @Operation(summary = "修改系统配置")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('sys:config:update')")
    @RepeatSubmit
    @Log(value = "修改系统配置", module = LogModuleEnum.SETTING)
    public Result<Void> updateConfig(@PathVariable Long id,
                                     @RequestBody @Valid ConfigForm formData) {
        return Result.judge(sysConfigService.updateConfig(id, formData));
    }

    /**
     * 批量删除配置。
     *
     * @param ids 配置 ID，多个用英文逗号分隔
     * @return 操作结果
     */
    @Operation(summary = "删除系统配置")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:config:delete')")
    @Log(value = "删除系统配置", module = LogModuleEnum.SETTING)
    public Result<Void> deleteConfigs(
            @Parameter(description = "配置ID，多个用英文逗号分隔") @PathVariable String ids) {
        return Result.judge(sysConfigService.deleteConfigs(ids));
    }
}
