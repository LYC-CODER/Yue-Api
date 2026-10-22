package com.lycorder.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lycorder.system.model.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
* @author LYCorder
* @description 针对表【sys_user_role(用户角色关联表)】的数据库操作Mapper
* @createDate 2026-01-23 19:26:46
* @Entity com.lycorder.model.entity.SysUserRole
*/
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    int countUsersByRoleId(Long roleId);
}




