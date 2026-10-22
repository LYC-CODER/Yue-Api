package com.lycorder.system.converter;

import com.lycorder.system.model.entity.SysMenu;
import com.lycorder.system.model.form.MenuForm;
import com.lycorder.system.model.vo.MenuVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 菜单对象转换器
 *
 * @author LYCoder
 */
@Mapper(componentModel = "spring")
public interface SysMenuConverter {

    MenuVO toVo(SysMenu entity);

    @Mapping(target = "params", ignore = true)
    MenuForm toForm(SysMenu entity);

    @Mapping(target = "params", ignore = true)
    SysMenu toEntity(MenuForm menuForm);

}