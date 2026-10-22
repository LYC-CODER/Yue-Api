package com.lycorder.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lycorder.common.model.Option;
import com.lycorder.system.model.entity.SysRole;
import com.lycorder.system.model.form.RoleForm;
import com.lycorder.system.model.vo.RolePageVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * 角色对象转换器
 *
 * @author LYCoder
 */
@Mapper(componentModel = "spring")
public interface SysRoleConverter {

    Page<RolePageVO> toPageVo(Page<SysRole> page);

    @Mappings({
            @Mapping(target = "value", source = "id"),
            @Mapping(target = "label", source = "name")
    })
    Option<Long> toOption(SysRole role);

    List<Option<Long>> toOptions(List<SysRole> roles);

    SysRole toEntity(RoleForm roleForm);

    RoleForm toForm(SysRole entity);
}