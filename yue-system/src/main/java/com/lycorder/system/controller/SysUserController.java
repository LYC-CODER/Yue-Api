package com.lycorder.system.controller;

import cn.idev.excel.EasyExcel;
import cn.idev.excel.ExcelWriter;
import com.lycorder.common.annotation.RepeatSubmit;
import com.lycorder.common.enums.LogModuleEnum;
import com.lycorder.common.model.Option;
import com.lycorder.common.util.ExcelUtils;
import com.lycorder.core.result.ExcelResult;
import com.lycorder.core.result.PageResult;
import com.lycorder.core.result.Result;
import com.lycorder.security.util.SecurityUtils;
import com.lycorder.system.listener.UserImportListener;
import com.lycorder.system.model.dto.CurrentUserDTO;
import com.lycorder.system.model.dto.UserExportDTO;
import com.lycorder.system.model.dto.UserImportDTO;
import com.lycorder.system.model.form.*;
import com.lycorder.system.model.query.UserQuery;
import com.lycorder.system.model.vo.UserPageVO;
import com.lycorder.system.model.vo.UserProfileVO;
import com.lycorder.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.lycorder.common.annotation.Log;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 用户管理接口，负责用户分页查询、增删改、导入导出以及个人中心信息维护。
 *
 * @projectName: Real-Time-Api
 * @package: com.lycorder.system.controller
 * @className: SysUserController
 * @author: LYCorder
 * @description: TODO
 * @date: 2026/3/8 20:56
 * @version: 1.0
 */
@Tag(name = "2.用户接口")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    /**
     * 分页查询用户列表。
     *
     * @param queryParams 用户查询参数
     * @return 用户分页数据
     */
    @Operation(summary = "用户列表")
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPerm('sys:user:list')")
    public PageResult<UserPageVO> getUserList(@Valid UserQuery queryParams) {
        return PageResult.success(sysUserService.getUserPage(queryParams));
    }

    /**
     * 新增用户，并保存其角色关联。
     *
     * @param userForm 用户表单数据
     * @return 操作结果
     */
    @Operation(summary = "新增用户")
    @PostMapping("/saveUser")
    @PreAuthorize("@ss.hasPerm('sys:user:create')")
    @RepeatSubmit
    @Log(value = "新增用户", module = LogModuleEnum.USER)
    public Result<?> saveUser(
            @RequestBody @Valid UserForm userForm
    ) {
        boolean result = sysUserService.saveUser(userForm);
        return Result.judge(result);
    }

    /**
     * 获取用户编辑表单数据。
     *
     * @param userId 用户ID
     * @return 用户表单数据
     */
    @Operation(summary = "获取用户表单数据")
    @GetMapping("/{userId}/form")
    @PreAuthorize("@ss.hasPerm('sys:user:update')")
    public Result<UserForm> getUserForm(
            @Parameter(description = "用户ID") @PathVariable Long userId
    ) {
        UserForm formData = sysUserService.getUserFormData(userId);
        return Result.success(formData);
    }

    /**
     * 修改指定用户信息。
     *
     * @param userId   用户ID
     * @param userForm 用户表单数据
     * @return 操作结果
     */
    @Operation(summary = "修改用户")
    @PutMapping(value = "/{userId}")
    @PreAuthorize("@ss.hasPerm('sys:user:update')")
    @Log(value = "修改用户", module = LogModuleEnum.USER)
    public Result<Void> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @RequestBody @Valid UserForm userForm
    ) {
        boolean result = sysUserService.updateUser(userId, userForm);
        return Result.judge(result);
    }

    /**
     * 批量删除用户。
     *
     * @param ids 用户ID字符串，多个以英文逗号分隔
     * @return 操作结果
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{ids}")
    @PreAuthorize("@ss.hasPerm('sys:user:delete')")
    @Log(value = "删除用户", module = LogModuleEnum.USER)
    public Result<Void> deleteUsers(
            @Parameter(description = "用户ID，多个以英文逗号(,)分割") @PathVariable String ids
    ) {
        boolean result = sysUserService.deleteUsers(ids);
        return Result.judge(result);
    }

    /**
     * 获取当前登录用户信息。
     *
     * @return 当前登录用户信息
     */
    @Operation(summary = "获取当前登录用户")
    @GetMapping("/me")
    public Result<CurrentUserDTO> getCurrentUser() {
        CurrentUserDTO currentUserDTO = sysUserService.getCurrentUserInfo();
        return Result.success(currentUserDTO);
    }

    /**
     * 下载用户导入模板。
     *
     * @param response HTTP响应对象
     */
    @Operation(summary = "用户导入模板下载")
    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response)  {
        String fileName = "用户导入模板.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        String fileClassPath = "templates" + File.separator + "excel" + File.separator + fileName;
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fileClassPath);

        try (ServletOutputStream outputStream = response.getOutputStream();
             ExcelWriter excelWriter = EasyExcel.write(outputStream).withTemplate(inputStream).build()) {
            excelWriter.finish();
        } catch (IOException e) {
            throw new RuntimeException("用户导入模板下载失败", e);
        }
    }


    /**
     * 导入用户数据。
     *
     * @param file 上传的Excel文件
     * @return 导入结果，包含成功、失败数量及错误信息
     * @throws IOException 读取上传文件失败时抛出
     */
    @Operation(summary = "导入用户")
    @PostMapping("/import")
    @PreAuthorize("@ss.hasPerm('sys:user:import')")
    @Log(value = "导入用户", module = LogModuleEnum.USER)
    public Result<ExcelResult> importUsers(MultipartFile file) throws IOException {
        // 使用 EasyExcel 监听器逐行解析并校验导入数据
        UserImportListener listener = new UserImportListener();
        ExcelUtils.importExcel(file.getInputStream(), UserImportDTO.class, listener);
        return Result.success(listener.getExcelResult());
    }


    /**
     * 导出用户数据。
     *
     * @param queryParams 用户查询参数
     * @param response    HTTP响应对象
     * @throws IOException 写出Excel文件失败时抛出
     */
    @Operation(summary = "导出用户")
    @GetMapping("/export")
    @PreAuthorize("@ss.hasPerm('sys:user:export')")
    @Log(value = "导出用户", module = LogModuleEnum.USER)
    public void exportUsers(UserQuery queryParams, HttpServletResponse response) throws IOException {
        String fileName = "用户列表.xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));

        // 查询导出数据并写出 Excel
        List<UserExportDTO> exportUserList = sysUserService.listExportUsers(queryParams);
        EasyExcel.write(response.getOutputStream(), UserExportDTO.class).sheet("用户列表")
                .doWrite(exportUserList);
    }

    /**
     * 获取当前登录用户的个人中心信息。
     *
     * @return 个人中心用户信息
     */
    @Operation(summary = "获取个人中心用户信息")
    @GetMapping("/profile")
    public Result<UserProfileVO> getUserProfile() {
        Long userId = SecurityUtils.getUserId();
        UserProfileVO userProfile = sysUserService.getUserProfile(userId);
        return Result.success(userProfile);
    }


    /**
     * 修改当前登录用户的个人中心信息。
     *
     * @param formData 个人中心表单数据
     * @return 操作结果
     */
    @Operation(summary = "个人中心修改用户信息")
    @PutMapping("/profile")
    @Log(value = "个人中心修改用户信息", module = LogModuleEnum.USER)
    public Result<?> updateUserProfile(@RequestBody UserProfileForm formData) {
        boolean result = sysUserService.updateUserProfile(formData);
        return Result.judge(result);
    }

    /**
     * 重置指定用户的密码。
     *
     * @param userId 用户ID
     * @param data   重置密码表单（密码放在请求体，避免出现在 URL 和访问日志中）
     * @return 操作结果
     */
    @Operation(summary = "重置指定用户密码")
    @PutMapping(value = "/{userId}/password/reset")
    @PreAuthorize("@ss.hasPerm('sys:user:reset-password')")
    @Log(value = "重置指定用户密码", module = LogModuleEnum.USER, params = false)
    public Result<?> resetUserPassword(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @RequestBody @Valid PasswordResetForm data
    ) {
        boolean result = sysUserService.resetUserPassword(userId, data.getPassword());
        return Result.judge(result);
    }


    /**
     * 当前登录用户修改密码。
     *
     * @param data 密码修改表单
     * @return 操作结果
     */
    @Operation(summary = "当前用户修改密码")
    @PutMapping(value = "/password")
    @Log(value = "当前用户修改密码", module = LogModuleEnum.USER, params = false)
    public Result<?> changeCurrentUserPassword(
            @RequestBody @Valid PasswordUpdateForm data
    ) {
        Long currUserId = SecurityUtils.getUserId();
        boolean result = sysUserService.changeUserPassword(currUserId, data);
        return Result.judge(result);
    }

    /**
     * 绑定或更换当前用户手机号。
     *
     * @param data 手机号及密码校验表单
     * @return 操作结果
     */
    @Operation(summary = "绑定或更换手机号")
    @PutMapping(value = "/mobile")
    @Log(value = "绑定或更换手机号", module = LogModuleEnum.USER, params = false)
    public Result<?> bindOrChangeMobile(
            @RequestBody @Validated MobileUpdateForm data
    ) {
        boolean result = sysUserService.bindOrChangeMobile(data);
        return Result.judge(result);
    }

    /**
     * 解绑当前用户手机号。
     *
     * @param data 密码校验表单
     * @return 操作结果
     */
    @Operation(summary = "解绑手机号")
    @DeleteMapping(value = "/mobile")
    @Log(value = "解绑手机号", module = LogModuleEnum.USER, params = false)
    public Result<?> unbindMobile(
            @RequestBody @Validated PasswordVerifyForm data
    ) {
        boolean result = sysUserService.unbindMobile(data);
        return Result.judge(result);
    }


    /**
     * 绑定或更换当前用户邮箱。
     *
     * @param data 邮箱及密码校验表单
     * @return 操作结果
     */
    @Operation(summary = "绑定或更换邮箱")
    @PutMapping(value = "/email")
    @Log(value = "绑定或更换邮箱", module = LogModuleEnum.USER, params = false)
    public Result<?> bindOrChangeEmail(
            @RequestBody @Validated EmailUpdateForm data
    ) {
        boolean result = sysUserService.bindOrChangeEmail(data);
        return Result.judge(result);
    }

    /**
     * 解绑当前用户邮箱。
     *
     * @param data 密码校验表单
     * @return 操作结果
     */
    @Operation(summary = "解绑邮箱")
    @DeleteMapping(value = "/email")
    @Log(value = "解绑邮箱", module = LogModuleEnum.USER, params = false)
    public Result<?> unbindEmail(
            @RequestBody @Validated PasswordVerifyForm data
    ) {
        boolean result = sysUserService.unbindEmail(data);
        return Result.judge(result);
    }


    /**
     * 获取用户下拉选项。
     *
     * @return 用户选项列表
     */
    @Operation(summary = "获取用户下拉选项")
    @GetMapping("/options")
    public Result<List<Option<String>>> listUserOptions() {
        List<Option<String>> list = sysUserService.listUserOptions();
        return Result.success(list);
    }

}
