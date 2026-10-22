package com.lycorder.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lycorder.common.constant.SystemConstants;
import com.lycorder.common.model.Option;
import com.lycorder.core.exception.BusinessException;
import com.lycorder.security.model.UserAuthInfo;
import com.lycorder.security.service.PermissionService;
import com.lycorder.security.token.TokenManager;
import com.lycorder.security.util.SecurityUtils;
import com.lycorder.system.converter.SysUserConverter;
import com.lycorder.system.enums.DictCodeEnum;
import com.lycorder.system.mapper.SysUserMapper;
import com.lycorder.system.model.bo.UserBO;
import com.lycorder.system.model.dto.CurrentUserDTO;
import com.lycorder.system.model.dto.UserExportDTO;
import com.lycorder.system.model.entity.SysDictItem;
import com.lycorder.system.model.entity.SysUser;
import com.lycorder.system.model.form.*;
import com.lycorder.system.model.query.UserQuery;
import com.lycorder.system.model.vo.UserPageVO;
import com.lycorder.system.model.vo.UserProfileVO;
import com.lycorder.system.service.SysDictItemService;
import com.lycorder.system.service.SysRoleService;
import com.lycorder.system.service.SysUserRoleService;
import com.lycorder.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
* @author LYCorder
* @description 针对表【sys_user(系统用户表)】的数据库操作Service实现
* @createDate 2026-01-23 19:26:46
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
    implements SysUserService {

    private final SysRoleService sysRoleService;
    private final PermissionService permissionService;
    private final SysUserConverter sysUserConverter;
    private final PasswordEncoder passwordEncoder;
    private final SysUserRoleService SysUserRoleService;
    private final SysDictItemService sysDictItemService;
    private final TokenManager tokenManager;

    private final StringRedisTemplate redisTemplate;

    /**
     * 根据用户名获取用户认证信息
     *
     * @param username 用户名
     * @return UserAuthInfo 用户认证信息
     */
    @Override
    public UserAuthInfo getAuthInfoByUsername(String username) {
        UserAuthInfo userAuthInfo = this.baseMapper.getAuthInfoByUsername(username);
        if (userAuthInfo != null) {
            Set<String> roles = userAuthInfo.getRoles();
            // 获取最大范围的数据权限
            Integer dataScope = sysRoleService.getMaximumDataScope(roles);
            userAuthInfo.setDataScope(dataScope);
        }
        return userAuthInfo;
    }

    /**
     * 获取当前登录用户信息
     *
     * @return CurrentUserDTO 当前登录用户信息
     */
    @Override
    public CurrentUserDTO getCurrentUserInfo() {

        String username = SecurityUtils.getUsername();

        // 获取登录用户基础信息
        SysUser sysUser = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .select(
                        SysUser::getId,
                        SysUser::getUsername,
                        SysUser::getNickname,
                        SysUser::getAvatar
                )
        );
        // entity->Vo
        CurrentUserDTO userInfoVo = sysUserConverter.toCurrentUserDto(sysUser);

        // 用户角色集合
        Set<String> roles = SecurityUtils.getRoles();
        userInfoVo.setRoles(roles);

        // 用户权限集合
        if (CollectionUtil.isNotEmpty(roles)) {
            Set<String> perms = permissionService.getRolePermsFormCache(roles);
            userInfoVo.setPerms(perms);
        }
        return userInfoVo;
    }

    /**
     * 获取用户列表
     *
     * @param queryParams 查询参数
     * @return 用户列表
     */
    @Override
    public IPage<UserPageVO> getUserPage(UserQuery queryParams) {

        // 参数构建
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        Page<UserBO> page = new Page<>(pageNum, pageSize);

        boolean isRoot = SecurityUtils.isRoot();
        queryParams.setIsRoot(isRoot);

        // 查询数据
        Page<UserBO> userPage = this.baseMapper.getUserPage(page, queryParams);

        // 实体转换
        return sysUserConverter.toPageVo(userPage);
    }


    /**
     * 保存用户
     *
     * @param userForm 用户表单
     * @return 是否保存成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveUser(UserForm userForm) {
        String username = userForm.getUsername();

        // 实体转换 form->entity
        SysUser entity = sysUserConverter.toEntity(userForm);

        // 检查用户名是否已存在
        long count = this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        Assert.isTrue(count == 0, "用户名已存在");

        // 设置默认加密密码
        String defaultEncryptPwd = passwordEncoder.encode(SystemConstants.DEFAULT_PASSWORD);
        entity.setPassword(defaultEncryptPwd);
        entity.setCreateBy(SecurityUtils.getUserId());

        // 新增用户
        boolean result = this.save(entity);

        if (result) {
            // 保存用户角色
            SysUserRoleService.saveUserRoles(entity.getId(), userForm.getRoleIds());
        }
        return result;    }

    /**
     * 获取用户表单数据
     *
     * @param userId 用户ID
     * @return 用户表单数据
     */
    @Override
    public UserForm getUserFormData(Long userId) {
        return this.baseMapper.getUserFormData(userId);
    }

    /**
     * 修改用户
     *
     * @param userId 用户ID
     * @param userForm 用户表单
     * @return 是否修改成功
     */
    @Override
    public boolean updateUser(Long userId, UserForm userForm) {

        String username = userForm.getUsername();

        // 获取原用户信息
        SysUser oldUser = this.getById(userId);
        Assert.notNull(oldUser, "用户不存在");

        // 检查用户名是否已存在（排除当前用户）
        long count = this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .ne(SysUser::getId, userId)
        );
        Assert.isTrue(count == 0, "用户名已存在");

        // form -> entity
        SysUser entity = sysUserConverter.toEntity(userForm);
        entity.setUpdateBy(SecurityUtils.getUserId());

        // 修改用户
        boolean result = this.updateById(entity);

        if (result) {
            // 保存用户角色
            SysUserRoleService.saveUserRoles(entity.getId(), userForm.getRoleIds());
        }
        return result;
    }


    /**
     * 删除用户
     *
     * @param idsStr 用户ID，多个以英文逗号(,)分割
     * @return true|false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUsers(String idsStr) {
        Assert.isTrue(StrUtil.isNotBlank(idsStr), "删除的用户数据为空");
        // 逻辑删除
        List<Long> ids = Arrays.stream(idsStr.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        boolean result = this.removeByIds(ids);
        return result;
    }

    @Override
    public List<UserExportDTO> listExportUsers(UserQuery queryParams) {
        boolean isRoot = SecurityUtils.isRoot();
        queryParams.setIsRoot(isRoot);

        List<UserExportDTO> exportUsers = this.baseMapper.listExportUsers(queryParams);
        if (CollectionUtil.isNotEmpty(exportUsers)) {
            //获取性别的字典项
            Map<String, String> genderMap = sysDictItemService.list(
                            new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictCode,
                                    DictCodeEnum.GENDER.getValue())
                    ).stream()
                    .collect(Collectors.toMap(SysDictItem::getValue, SysDictItem::getLabel)
                    );

            exportUsers.forEach(item -> {
                String gender = item.getGender();
                if (StrUtil.isBlank(gender)) {
                    return;
                }

                // 判断map是否为空
                if (genderMap.isEmpty()) {
                    return;
                }

                item.setGender(genderMap.get(gender));
            });
        }
        return exportUsers;
    }

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        UserBO entity = this.baseMapper.getUserProfile(userId);
        return sysUserConverter.toProfileVo(entity);
    }

    @Override
    public boolean updateUserProfile(UserProfileForm formData) {
        Long userId = SecurityUtils.getUserId();
        SysUser entity = sysUserConverter.toEntity(formData);
        entity.setId(userId);
        return this.updateById(entity);
    }

    @Override
    public boolean resetUserPassword(Long userId, String password) {

        boolean result = this.update(new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .set(SysUser::getPassword, passwordEncoder.encode(password))
        );
        if (result) {
            // 管理员重置用户密码后，使该用户的所有会话失效
            tokenManager.invalidateUserSessions(userId);
        }
        return result;
    }

    @Override
    public boolean changeUserPassword(Long userId, PasswordUpdateForm data) {

        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String oldPassword = data.getOldPassword();

        // 校验原密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        // 新旧密码不能相同
        if (passwordEncoder.matches(data.getNewPassword(), user.getPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }

        // 判断新密码和确认密码是否一致
        if (passwordEncoder.matches(data.getNewPassword(), data.getConfirmPassword())) {
            throw new BusinessException("新密码和确认密码不一致");
        }

        String newPassword = data.getNewPassword();
        boolean result = this.update(new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .set(SysUser::getPassword, passwordEncoder.encode(newPassword))
        );

        if (result) {
            // 密码变更后，使当前用户的所有会话失效，强制重新登录
            tokenManager.invalidateUserSessions(userId);
        }
        return result;
    }

    @Override
    public boolean bindOrChangeMobile(MobileUpdateForm form) {

        Long currentUserId = SecurityUtils.getUserId();
        SysUser currentUser = this.getById(currentUserId);

        if (currentUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验密码
        String inputPassword = form.getPassword();
        if (!passwordEncoder.matches(inputPassword, currentUser.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 检查手机号是否已被其他用户使用
        long count = this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getMobile, form.getMobile())
                .ne(SysUser::getId, currentUserId));
        if (count > 0) {
            throw new BusinessException("手机号已被其他用户使用");
        }

        // 更新手机号码
        return this.update(
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, currentUserId)
                        .set(SysUser::getMobile, form.getMobile())
        );

    }

    @Override
    public boolean unbindMobile(PasswordVerifyForm data) {
        Long currentUserId = SecurityUtils.getUserId();
        SysUser currentUser = this.getById(currentUserId);

        if (currentUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验密码
        if (!passwordEncoder.matches(data.getPassword(), currentUser.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 解绑手机号
        return this.update(
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, currentUserId)
                        .set(SysUser::getMobile, null)
        );
    }

    @Override
    public boolean bindOrChangeEmail(EmailUpdateForm data) {
        Long currentUserId = SecurityUtils.getUserId();
        SysUser currentUser = this.getById(currentUserId);

        if (currentUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验密码
        if (!passwordEncoder.matches(data.getPassword(), currentUser.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 检查邮箱是否已被其他用户使用
        SysUser existingUser = this.getOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getEmail, data.getEmail())
                        .ne(SysUser::getId, currentUserId)
        );

        if (existingUser != null) {
            throw new BusinessException("该邮箱已被使用");
        }

        // 绑定或更新邮箱
        return this.update(
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, currentUserId)
                        .set(SysUser::getEmail, data.getEmail())
        );
    }

    @Override
    public boolean unbindEmail(PasswordVerifyForm data) {
        Long currentUserId = SecurityUtils.getUserId();
        SysUser currentUser = this.getById(currentUserId);

        if (currentUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验密码
        if (!passwordEncoder.matches(data.getPassword(), currentUser.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 解绑邮箱
        return this.update(
                new LambdaUpdateWrapper<SysUser>()
                        .eq(SysUser::getId, currentUserId)
                        .set(SysUser::getEmail, null)
        );
    }

    @Override
    public List<Option<String>> listUserOptions() {
        List<SysUser> list = this.list(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, 1)
        );
        return sysUserConverter.toOptions(list);
    }
}




