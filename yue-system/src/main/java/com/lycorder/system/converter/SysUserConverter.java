package com.lycorder.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lycorder.common.model.Option;
import com.lycorder.system.model.bo.UserBO;
import com.lycorder.system.model.dto.CurrentUserDTO;
import com.lycorder.system.model.dto.UserImportDTO;
import com.lycorder.system.model.entity.SysUser;
import com.lycorder.system.model.form.UserForm;
import com.lycorder.system.model.form.UserProfileForm;
import com.lycorder.system.model.vo.UserPageVO;
import com.lycorder.system.model.vo.UserProfileVO;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

/**
 * 用户对象转换器
 *
 * @author LYCoder
 */
@Mapper(componentModel = "spring")
public interface SysUserConverter {

    UserPageVO toPageVo(UserBO bo);

    Page<UserPageVO> toPageVo(Page<UserBO> bo);

    List<UserPageVO> toPageVo(List<UserBO> bo);

    UserForm toForm(SysUser entity);

    @InheritInverseConfiguration(name = "toForm")
    SysUser toEntity(UserForm entity);

    @Mappings({
            @Mapping(target = "userId", source = "id")
    })
    CurrentUserDTO toCurrentUserDto(SysUser entity);

    SysUser toEntity(UserImportDTO vo);


    UserProfileVO toProfileVo(UserBO bo);

    SysUser toEntity(UserProfileForm formData);

    @Mappings({
            @Mapping(target = "label", source = "nickname"),
            @Mapping(target = "value", source = "id")
    })
    Option<String> toOption(SysUser entity);

    List<Option<String>> toOptions(List<SysUser> list);
}
