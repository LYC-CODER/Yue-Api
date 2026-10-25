package com.lycorder.system.controller;

import com.lycorder.common.annotation.RepeatSubmit;
import com.lycorder.common.enums.LogModuleEnum;
import com.lycorder.common.model.Option;
import com.lycorder.core.result.Result;
import com.lycorder.system.model.form.DeptForm;
import com.lycorder.system.model.query.DeptQuery;
import com.lycorder.system.model.vo.DeptVO;
import com.lycorder.system.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.lycorder.common.annotation.Log;

import java.util.List;

/**
 * 部门管理接口，负责部门树查询、增删改及下拉选项维护。
 *
 * @projectName: Real-Time-Api
 * @package: com.lycorder.system.controller
 * @className: SysDeptController
 * @author: LYCorder
 * @description: TODO
 * @date: 2026/3/10 19:53
 * @version: 1.0
 */

@Tag(name = "5.部门接口")
@RestController
@RequestMapping("/api/v1/depts")
@RequiredArgsConstructor
public class SysDeptController {
    private final SysDeptService sysDeptService;

    /**
     * 查询部门树列表。
     *
     * @param queryParams 部门查询参数
     * @return 部门树列表
     */
    @Operation(summary = "部门列表")
    @GetMapping
    @Log( value = "部门列表",module = LogModuleEnum.DEPT)
    public Result<List<DeptVO>> getDeptList(
            DeptQuery queryParams
    ) {
        List<DeptVO> list = sysDeptService.getDeptList(queryParams);
        return Result.success(list);
    }

    /**
     * 获取部门下拉选项。
     *
     * @return 部门下拉选项
     */
    @Operation(summary = "部门下拉列表")
    @GetMapping("/options")
    @Log( value = "部门下拉列表",module = LogModuleEnum.DEPT)
    public Result<List<Option<Long>>> getDeptOptions() {
        List<Option<Long>> list = sysDeptService.listDeptOptions();
        return Result.success(list);
    }

    /**
     * 新增部门。
     *
     * @param formData 部门表单数据
     * @return 新增部门ID
     */
    @Operation(summary = "新增部门")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:dept:create')")
    @RepeatSubmit
    @Log( value = "新增部门",module = LogModuleEnum.DEPT)
    public Result<?> saveDept(
            @Valid @RequestBody DeptForm formData
    ) {
        Long id = sysDeptService.saveDept(formData);
        return Result.success(id);
    }


    /**
     * 获取部门编辑表单数据。
     *
     * @param deptId 部门ID
     * @return 部门表单数据
     */
    @Operation(summary = "获取部门表单数据")
    @GetMapping("/{deptId}/form")
    @PreAuthorize("@ss.hasPerm('sys:dept:update')")
    public Result<DeptForm> getDeptForm(
            @Parameter(description ="部门ID") @PathVariable Long deptId
    ) {
        DeptForm deptForm = sysDeptService.getDeptForm(deptId);
        return Result.success(deptForm);
    }

    /**
     * 修改部门信息。
     *
     * @param deptId   部门ID
     * @param formData 部门表单数据
     * @return 修改后的部门ID
     */
    @Operation(summary = "修改部门")
    @PutMapping(value = "/{deptId}")
    @PreAuthorize("@ss.hasPerm('sys:dept:update')")
    @Log( value = "修改部门",module = LogModuleEnum.DEPT)
    public Result<?> updateDept(
            @PathVariable Long deptId,
            @Valid @RequestBody DeptForm formData
    ) {
        deptId = sysDeptService.updateDept(deptId, formData);
        return Result.success(deptId);
    }

    /**
     * 批量删除部门及其子部门。
     *
     * @param ids 部门ID字符串，多个以英文逗号分隔
     * @return 操作结果
     */
    @Operation(summary = "删除部门")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:dept:delete')")
    @Log( value = "删除部门",module = LogModuleEnum.DEPT)
    public Result<?> deleteDepartments(
            @Parameter(description ="部门ID，多个以英文逗号(,)分割") @PathVariable("ids") String ids
    ) {
        boolean result = sysDeptService.deleteByIds(ids);
        return Result.judge(result);
    }
}
