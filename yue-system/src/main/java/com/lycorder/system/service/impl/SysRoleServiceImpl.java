package com.lycorder.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lycorder.common.constant.SystemConstants;
import com.lycorder.common.model.Option;
import com.lycorder.core.exception.BusinessException;
import com.lycorder.security.util.SecurityUtils;
import com.lycorder.system.converter.SysRoleConverter;
import com.lycorder.system.mapper.SysRoleMapper;
import com.lycorder.system.model.entity.SysRole;
import com.lycorder.system.model.entity.SysRoleMenu;
import com.lycorder.system.model.form.RoleForm;
import com.lycorder.system.model.query.RoleQuery;
import com.lycorder.system.model.vo.RolePageVO;
import com.lycorder.system.service.SysRoleMenuService;
import com.lycorder.system.service.SysRoleService;
import com.lycorder.system.service.SysUserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;


/**
* @author LYCorder
* @description 针对表【sys_role(系统角色表)】的数据库操作Service实现
* @createDate 2026-01-23 19:26:46
*/
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
    implements SysRoleService{

    private final SysRoleMenuService sysRoleMenuService;
    private final SysRoleConverter sysRoleConverter;
    private final SysUserRoleService sysUserRoleService;


    /**
     * 获取最大数据权限
     *
     * @param roles 角色集合
     * @return 最大数据权限
     */
    @Override
    public Integer getMaximumDataScope(Set<String> roles) {
        Integer dataScope = this.baseMapper.getMaximumDataScope(roles);
        return dataScope;
    }

    /**
     * 角色分页列表
     *
     * @param queryParams 查询参数
     * @return 角色分页列表
     */
    @Override
    public Page<RolePageVO> getRolePage(RoleQuery queryParams) {

        // 查询参数
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        String keywords = queryParams.getKeywords();

        // 查询数据
        Page<SysRole> rolePage = this.page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysRole>()
                        .and(StrUtil.isNotBlank(keywords),
                                wrapper ->
                                        wrapper.like(SysRole::getName, keywords)
                                                .or()
                                                .like(SysRole::getCode, keywords)
                        )
                        .ne(!SecurityUtils.isRoot(), SysRole::getCode, SystemConstants.ROOT_ROLE_CODE) // 非超级管理员不显示超级管理员角色
                        .orderByAsc(SysRole::getSort).orderByDesc(SysRole::getCreateTime).orderByDesc(SysRole::getUpdateTime)
        );

        // 实体转换
        return sysRoleConverter.toPageVo(rolePage);
    }

    /**
     * 角色选项列表
     *
     * @return 角色选项列表
     */
    @Override
    public List<Option<Long>> listRoleOptions() {
        // 查询数据
        List<SysRole> roleList = this.list(new LambdaQueryWrapper<SysRole>()
                .ne(!SecurityUtils.isRoot(), SysRole::getCode, SystemConstants.ROOT_ROLE_CODE)
                .select(SysRole::getId, SysRole::getName)
                .orderByAsc(SysRole::getSort)
        );
        // 实体转换
        return sysRoleConverter.toOptions(roleList);
    }

    @Override
    public boolean saveRole(RoleForm roleForm) {

        Long roleId = roleForm.getId();

        // 编辑角色时，判断角色是否存在
        SysRole oldRole = null;
        if (roleId != null) {
            oldRole = this.getById(roleId);
            Assert.isTrue(oldRole != null, "角色不存在");
        }

        String roleCode = roleForm.getCode();
        long count = this.count(new LambdaQueryWrapper<SysRole>()
                .ne(roleId != null, SysRole::getId, roleId)
                .and(wrapper ->
                        wrapper.eq(SysRole::getCode, roleCode).or().eq(SysRole::getName, roleForm.getName())
                ));
        Assert.isTrue(count == 0, "角色名称或角色编码已存在，请修改后重试！");

        // 实体转换
        SysRole role = sysRoleConverter.toEntity(roleForm);

        boolean result = this.saveOrUpdate(role);
        if (result) {
            // 判断角色编码或状态是否修改，修改了则刷新权限缓存
            if (oldRole != null
                    && (
                    !StrUtil.equals(oldRole.getCode(), roleCode) ||
                            !ObjectUtil.equals(oldRole.getStatus(), roleForm.getStatus())
            )) {
                sysRoleMenuService.refreshRolePermsCache(oldRole.getCode(), roleCode);
            }
        }
        return result;
    }

    /**
     * 获取角色表单
     *
     * @param roleId 角色ID
     * @return 角色表单
     */
    @Override
    public RoleForm getRoleForm(Long roleId) {
        SysRole entity = this.getById(roleId);
        return sysRoleConverter.toForm(entity);
    }

    /**
     * 删除角色
     *
     * @param ids 角色ID，多个以英文逗号(,)分割
     *  */
    @Override
    public void deleteRoles(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的角色ID不能为空");
        List<Long> roleIds = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();

        for (Long roleId : roleIds) {
            SysRole role = this.getById(roleId);
            Assert.isTrue(role != null, "角色不存在");

            // 判断角色是否被用户关联
            boolean isRoleAssigned = sysUserRoleService.hasAssignedUsers(roleId);
            Assert.isTrue(!isRoleAssigned, "角色【{}】已分配用户，请先解除关联后删除", role.getName());

            boolean deleteResult = this.removeById(roleId);
            if (deleteResult) {
                // 删除成功，刷新权限缓存
                sysRoleMenuService.refreshRolePermsCache(role.getCode());
            }
        }

    }

    /**
     * 更新角色状态
     *
     * @param roleId 角色ID
     * @param status 角色状态(1-正常 0-停用)
     * @return 是否成功
     */
    @Override
    public boolean updateRoleStatus(Long roleId, Integer status) {

        SysRole role = this.getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        role.setStatus(status);
        boolean result = this.updateById(role);
        if (result) {
            // 刷新角色的权限缓存
            sysRoleMenuService.refreshRolePermsCache(role.getCode());
        }
        return result;
    }

    /**
     * 获取角色菜单ID列表
     * @param roleId 角色ID
     * @return 角色菜单ID列表
     *  */
    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return sysRoleMenuService.listMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional
    public void assignMenusToRole(Long roleId, List<Long> menuIds) {
        SysRole role = this.getById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        // 删除角色菜单
        sysRoleMenuService.remove(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId)
        );
        // 新增角色菜单
        if (CollectionUtil.isNotEmpty(menuIds)) {
            List<SysRoleMenu> roleMenus = menuIds
                    .stream()
                    .map(menuId -> new SysRoleMenu(roleId, menuId))
                    .toList();
            sysRoleMenuService.saveBatch(roleMenus);
        }

        // 刷新角色的权限缓存
        sysRoleMenuService.refreshRolePermsCache(role.getCode());
    }
}




