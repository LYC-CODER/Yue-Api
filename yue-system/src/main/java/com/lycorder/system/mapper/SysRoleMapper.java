package com.lycorder.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lycorder.system.model.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
* @author LYCorder
* @description 针对表【sys_role(系统角色表)】的数据库操作Mapper
* @createDate 2026-01-23 19:26:46
* @Entity com.lycorder.model.entity.SysRole
*/
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    Integer getMaximumDataScope(Set<String> roles);
}




