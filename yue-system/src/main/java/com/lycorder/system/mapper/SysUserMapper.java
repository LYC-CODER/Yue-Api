package com.lycorder.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lycorder.common.annotation.DataPermission;
import com.lycorder.security.model.UserAuthInfo;
import com.lycorder.system.model.bo.UserBO;
import com.lycorder.system.model.dto.UserExportDTO;
import com.lycorder.system.model.entity.SysUser;
import com.lycorder.system.model.form.UserForm;
import com.lycorder.system.model.query.UserQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

    @DataPermission(
            deptAlias = "u",
            deptIdColumnName = "dept_id",
            userAlias = "u",
            userIdColumnName = "id"
    )
    Page<UserBO> getUserPage(Page<UserBO> page, @Param("queryParams") UserQuery queryParams);

    UserForm getUserFormData(Long userId);

    @DataPermission(
            deptAlias = "u",
            deptIdColumnName = "dept_id",
            userAlias = "u",
            userIdColumnName = "id"
    )
    List<UserExportDTO> listExportUsers(UserQuery queryParams);

    UserBO getUserProfile(Long userId);

    /**
     * 统计用户是否拥有超级管理员角色。
     *
     * @param userId 用户ID
     * @return 拥有 ROOT 角色的关联数量
     */
    @Select("""
            SELECT COUNT(1)
            FROM sys_user_role ur
            INNER JOIN sys_role r ON ur.role_id = r.id AND r.is_deleted = 0
            WHERE ur.user_id = #{userId}
              AND r.code = 'ROOT'
            """)
    int countRootRoleByUserId(@Param("userId") Long userId);
}




