package com.lycorder.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lycorder.common.model.Option;
import com.lycorder.security.model.UserAuthInfo;
import com.lycorder.system.model.dto.CurrentUserDTO;
import com.lycorder.system.model.dto.UserExportDTO;
import com.lycorder.system.model.entity.SysUser;
import com.lycorder.system.model.form.*;
import com.lycorder.system.model.query.UserQuery;
import com.lycorder.system.model.vo.UserPageVO;
import com.lycorder.system.model.vo.UserProfileVO;
import jakarta.validation.Valid;

import java.util.List;

/**
* @author LYCorder
* @description 针对表【sys_user(系统用户表)】的数据库操作Service
* @createDate 2026-01-23 19:26:46
*/
public interface SysUserService extends IService<SysUser> {

    /**
     * 根据用户名获取认证信息
     *
     * @param username 用户名
     * @return 认证信息
     */
    UserAuthInfo getAuthInfoByUsername(String username);

    /**
     * 获取当前登录用户信息
     *
     * @return 当前登录用户信息
     */
    CurrentUserDTO getCurrentUserInfo();

    /**
     * 获取用户列表
     *
     * @param queryParams 查询参数
     * @return 用户列表
     */
    IPage<UserPageVO> getUserPage(UserQuery queryParams);

    /**
     * 新增用户
     *
     * @param userForm 用户表单
     * @return 是否成功
     */
    boolean saveUser(@Valid UserForm userForm);

    /**
     * 获取用户表单数据
     *
     * @param userId 用户ID
     * @return 用户表单数据
     */
    UserForm getUserFormData(Long userId);

    /**
     * 更新用户
     *
     * @param userId 用户ID
     * @param userForm 用户表单
     * @return 是否成功
     */
    boolean updateUser(Long userId, @Valid UserForm userForm);


    /**
     * 删除用户
     *
     * @param idsStr 用户ID，多个以英文逗号(,)分割
     * @return true|false
     */
    boolean deleteUsers(String idsStr);

    List<UserExportDTO> listExportUsers(UserQuery queryParams);

    UserProfileVO getUserProfile(Long userId);

    boolean updateUserProfile(UserProfileForm formData);

    boolean resetUserPassword(Long userId, String password);

    boolean changeUserPassword(Long userId, PasswordUpdateForm data);

    boolean bindOrChangeMobile(MobileUpdateForm data);

    boolean unbindMobile(PasswordVerifyForm data);

    boolean bindOrChangeEmail(EmailUpdateForm data);

    boolean unbindEmail(PasswordVerifyForm data);

    List<Option<String>> listUserOptions();
}
