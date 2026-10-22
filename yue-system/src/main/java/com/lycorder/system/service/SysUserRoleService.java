package com.lycorder.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lycorder.system.model.entity.SysUserRole;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;


/**
* @author LYCorder
* @description 针对表【sys_user_role(用户角色关联表)】的数据库操作Service
* @createDate 2026-01-23 19:26:46
*/
public interface SysUserRoleService extends IService<SysUserRole> {

    /**
     * 保存用户角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID集合
     */
    void saveUserRoles(Long userId, @NotEmpty(message = "用户角色不能为空") List<Long> roleIds);

    /**
     * 判断角色是否被用户关联
     *
     * @param roleId 角色ID
     * @return 是否被用户关联
     */
    boolean hasAssignedUsers(Long roleId);
}
