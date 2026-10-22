package com.lycorder.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lycorder.common.annotation.Log;
import com.lycorder.common.annotation.RepeatSubmit;
import com.lycorder.common.enums.LogModuleEnum;
import com.lycorder.common.model.Option;
import com.lycorder.core.result.PageResult;
import com.lycorder.core.result.Result;
import com.lycorder.system.model.form.DictForm;
import com.lycorder.system.model.form.DictItemForm;
import com.lycorder.system.model.query.DictItemQuery;
import com.lycorder.system.model.query.DictQuery;
import com.lycorder.system.model.vo.DictItemOptionVO;
import com.lycorder.system.model.vo.DictItemPageVO;
import com.lycorder.system.model.vo.DictPageVO;
import com.lycorder.system.service.SysDictItemService;
import com.lycorder.system.service.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * @projectName: Real-Time-Api
 * @package: com.lycorder.system.controller
 * @className: SysDictController
 * @author: LYCorder
 * @description: TODO
 * @date: 2026/3/11 17:35
 * @version: 1.0
 */

@Tag(name = "6.字典接口")
@RestController
@RequestMapping("/api/v1/dicts")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService sysDictService;
    private final SysDictItemService sysDictItemService;


    //---------------------------------------------------//
    // 字典相关接口
    //---------------------------------------------------//
    @Operation(summary = "字典分页列表")
    @GetMapping
    @Log(value = "字典分页列表",module = LogModuleEnum.DICT)
    public PageResult<DictPageVO> getDictPage(
            DictQuery queryParams
    ) {
        Page<DictPageVO> result = sysDictService.getDictPage(queryParams);
        return PageResult.success(result);
    }

    @Operation(summary = "字典列表")
    @GetMapping("/options")
    @Log(value = "字典列表",module = LogModuleEnum.DICT)
    public Result<List<Option<String>>> getDictList() {
        List<Option<String>> list = sysDictService.getDictList();
        return Result.success(list);
    }

    @Operation(summary = "获取字典表单数据")
    @GetMapping("/{id}/form")
    @Log(value = "获取字典表单数据",module = LogModuleEnum.DICT)
    public Result<DictForm> getDictForm(
            @Parameter(description = "字典ID") @PathVariable Long id
    ) {
        DictForm formData = sysDictService.getDictForm(id);
        return Result.success(formData);
    }

    @Operation(summary = "新增字典")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('sys:dict:create')")
    @RepeatSubmit
    @Log(value = "新增字典",module = LogModuleEnum.DICT)
    public Result<?> saveDict(@Valid @RequestBody DictForm formData) {
        boolean result = sysDictService.saveDict(formData);
        return Result.judge(result);
    }

    @Operation(summary = "修改字典")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('sys:dict:update')")
    @Log(value = "修改字典",module = LogModuleEnum.DICT)
    public Result<?> updateDict(
            @PathVariable Long id,
            @RequestBody DictForm dictForm
    ) {
        boolean status = sysDictService.updateDict(id, dictForm);
        return Result.judge(status);
    }

    @Operation(summary = "删除字典")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:dict:delete')")
    @Log(value = "删除字典",module = LogModuleEnum.DICT)
    public Result<?> deleteDictionaries(
            @Parameter(description = "字典ID，多个以英文逗号(,)拼接") @PathVariable String ids
    ) {
        // 获取字典编码列表，用于发送删除通知
        List<String> dictCodes = sysDictService.getDictCodesByIds(Arrays.stream(ids.split(",")).toList());

        sysDictService.deleteDictByIds(Arrays.stream(ids.split(",")).toList());

        return Result.success();
    }


    //---------------------------------------------------//
    // 字典项相关接口
    //---------------------------------------------------//


    //---------------------------------------------------
    // 字典项相关接口
    //---------------------------------------------------
    @Operation(summary = "字典项分页列表")
    @GetMapping("/{dictCode}/items")
    @Log(value = "字典项分页列表",module = LogModuleEnum.DICT)
    public PageResult<DictItemPageVO> getDictItemPage(
            @PathVariable String dictCode,
            DictItemQuery queryParams
    ) {
        queryParams.setDictCode(dictCode);
        Page<DictItemPageVO> result = sysDictItemService.getDictItemPage(queryParams);
        return PageResult.success(result);
    }


    @Operation(summary = "字典项列表")
    @GetMapping("/{dictCode}/items/options")
    @Log(value = "字典项列表",module = LogModuleEnum.DICT)
    public Result<List<DictItemOptionVO>> getDictItems(
            @Parameter(description = "字典编码") @PathVariable String dictCode
    ) {
        List<DictItemOptionVO> list = sysDictItemService.getDictItems(dictCode);
        return Result.success(list);
    }


    @Operation(summary = "新增字典项")
    @PostMapping("/{dictCode}/items")
    @PreAuthorize("@ss.hasPerm('sys:dict-item:create')")
    @RepeatSubmit
    @Log(value = "新增字典项",module = LogModuleEnum.DICT)
    public Result<Void> saveDictItem(
            @PathVariable String dictCode,
            @Valid @RequestBody DictItemForm formData
    ) {
        formData.setDictCode(dictCode);
        boolean result = sysDictItemService.saveDictItem(formData);

        return Result.judge(result);
    }


    @Operation(summary = "字典项表单数据")
    @GetMapping("/{dictCode}/items/{itemId}/form")
    @Log(value = "字典项表单数据",module = LogModuleEnum.DICT)
    public Result<DictItemForm> getDictItemForm(
            @PathVariable String dictCode,
            @Parameter(description = "字典项ID") @PathVariable Long itemId
    ) {
        DictItemForm formData = sysDictItemService.getDictItemForm(itemId);
        return Result.success(formData);
    }


    @Operation(summary = "修改字典项")
    @PutMapping("/{dictCode}/items/{itemId}")
    @PreAuthorize("@ss.hasPerm('sys:dict-item:update')")
    @RepeatSubmit
    @Log(value = "修改字典项",module = LogModuleEnum.DICT)
    public Result<?> updateDictItem(
            @PathVariable String dictCode,
            @PathVariable Long itemId,
            @RequestBody DictItemForm formData
    ) {
        formData.setId(itemId);
        formData.setDictCode(dictCode);
        boolean status = sysDictItemService.updateDictItem(formData);

        return Result.judge(status);
    }


    @Operation(summary = "删除字典项")
    @DeleteMapping("/{dictCode}/items/{itemIds}")
    @PreAuthorize("@ss.hasPerm('sys:dict-item:delete')")
    @Log(value = "删除字典项",module = LogModuleEnum.DICT)
    public Result<Void> deleteDictItems(
            @PathVariable String dictCode,
            @Parameter(description = "字典ID，多个以英文逗号(,)拼接") @PathVariable String itemIds
    ) {
        sysDictItemService.deleteDictItemByIds(itemIds);

        return Result.success();
    }

}
