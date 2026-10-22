package com.lycorder.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lycorder.security.model.UserAuthInfo;
import com.lycorder.system.model.bo.UserBO;
import com.lycorder.system.model.dto.UserExportDTO;
import com.lycorder.system.model.entity.SysUser;
import com.lycorder.system.model.form.UserForm;
import com.lycorder.system.model.query.UserQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author LYCorder
* @description 针对表【sys_user(系统用户表)】的数据库操作Mapper
* @createDate 2026-01-23 19:26:46
* @Entity com.lycorder.system.model.entity.SysUser;
*/
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    UserAuthInfo getAuthInfoByUsername(String username);

    Page<UserBO> getUserPage(Page<UserBO> page, @Param("queryParams") UserQuery queryParams);

    UserForm getUserFormData(Long userId);

    List<UserExportDTO> listExportUsers(UserQuery queryParams);

    UserBO getUserProfile(Long userId);
}




