package com.lycorder.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lycorder.system.model.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Set;

/**
* @author LYCorder
* @description 针对表【sys_menu(系统菜单表)】的数据库操作Mapper
* @createDate 2026-01-23 19:26:46
* @Entity com.lycorder.model.entity.SysMenu
*/
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> getMenusByRoleCodes(Set<String> roleCodes);
}




