package com.lycorder.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lycorder.security.token.TokenManager;
import com.lycorder.system.mapper.SysUserRoleMapper;
import com.lycorder.system.model.entity.SysUserRole;
import com.lycorder.system.service.SysUserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author LYCorder
* @description 针对表【sys_user_role(用户角色关联表)】的数据库操作Service实现
* @createDate 2026-01-23 19:26:46
*/
@Service
@RequiredArgsConstructor
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole>
    implements SysUserRoleService {

    private final TokenManager tokenManager;

    /**
     * 保存用户角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    @Override
    public void saveUserRoles(Long userId, List<Long> roleIds) {
        if (userId == null || CollectionUtil.isEmpty(roleIds)) {
            return ;
        }

        // 获取现有角色
        List<Long> userRoleIds = this.list(new LambdaQueryWrapper<SysUserRole>()
                        .select(SysUserRole::getRoleId)
                        .eq(SysUserRole::getUserId, userId))
                .parallelStream()
                .map(SysUserRole::getRoleId)
                .toList();

        // 使用Set提升对比效率
        Set<Long> oldRoles = new HashSet<>(userRoleIds);
        Set<Long> newRoles = new HashSet<>(roleIds);

        // 计算变更集
        Set<Long> addedRoles = new HashSet<>(newRoles);
        addedRoles.removeAll(oldRoles);

        Set<Long> removedRoles = new HashSet<>(oldRoles);
        removedRoles.removeAll(newRoles);

        boolean rolesChanged = !addedRoles.isEmpty() || !removedRoles.isEmpty();

        // 批量保存新增角色
        if (!addedRoles.isEmpty()) {
            this.saveBatch(addedRoles.stream()
                    .map(roleId -> new SysUserRole(userId, roleId))
                    .collect(Collectors.toList()));
        }

        // 删除废弃角色
        if (!removedRoles.isEmpty()) {
            this.remove(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, userId)
                    .in(SysUserRole::getRoleId, removedRoles));
        }

        // 当权限变更时清除被修改用户的登录态
        if (rolesChanged) {
            tokenManager.invalidateUserSessions(userId);
        }
    }

    @Override
    public boolean hasAssignedUsers(Long roleId) {
        int count = this.baseMapper.countUsersByRoleId(roleId);
        return count > 0;
    }
}




