package com.lycorder.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lycorder.system.model.bo.RolePermsBO;
import com.lycorder.system.model.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

/**
* @author LYCorder
* @description 针对表【sys_role_menu(角色菜单关联表)】的数据库操作Mapper
* @createDate 2026-01-23 19:26:46
* @Entity com.lycorder.model.entity.SysRoleMenu
*/
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    List<RolePermsBO> getRolePermsList(Object o);

    Set<String> listRolePerms(Set<String> roles);

    List<Long> listMenuIdsByRoleId(Long roleId);
}




