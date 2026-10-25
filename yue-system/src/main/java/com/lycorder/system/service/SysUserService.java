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
* 用户业务接口，覆盖认证信息查询、用户管理、个人中心及账号绑定等操作。
*
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

    /**
     * 获取用户导出数据。
     *
     * @param queryParams 用户查询参数
     * @return 用户导出数据列表
     */
    List<UserExportDTO> listExportUsers(UserQuery queryParams);

    /**
     * 获取用户个人中心信息。
     *
     * @param userId 用户ID
     * @return 个人中心用户信息
     */
    UserProfileVO getUserProfile(Long userId);

    /**
     * 修改用户个人中心信息。
     *
     * @param formData 个人中心表单数据
     * @return 是否修改成功
     */
    boolean updateUserProfile(UserProfileForm formData);

    /**
     * 重置指定用户密码。
     *
     * @param userId   用户ID
     * @param password 新密码（明文）
     * @return 是否重置成功
     */
    boolean resetUserPassword(Long userId, String password);

    /**
     * 修改用户密码。
     *
     * @param userId 用户ID
     * @param data   密码修改表单
     * @return 是否修改成功
     */
    boolean changeUserPassword(Long userId, PasswordUpdateForm data);

    /**
     * 绑定或更换手机号。
     *
     * @param data 手机号及密码校验表单
     * @return 是否操作成功
     */
    boolean bindOrChangeMobile(MobileUpdateForm data);

    /**
     * 解绑手机号。
     *
     * @param data 密码校验表单
     * @return 是否解绑成功
     */
    boolean unbindMobile(PasswordVerifyForm data);

    /**
     * 绑定或更换邮箱。
     *
     * @param data 邮箱及密码校验表单
     * @return 是否操作成功
     */
    boolean bindOrChangeEmail(EmailUpdateForm data);

    /**
     * 解绑邮箱。
     *
     * @param data 密码校验表单
     * @return 是否解绑成功
     */
    boolean unbindEmail(PasswordVerifyForm data);

    /**
     * 获取用户下拉选项。
     *
     * @return 用户选项列表
     */
    List<Option<String>> listUserOptions();
}
