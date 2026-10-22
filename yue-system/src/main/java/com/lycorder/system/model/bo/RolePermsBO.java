package com.lycorder.system.model.bo;

import lombok.Data;

import java.util.Set;

/**
 * 角色权限集合
 *
 * @author LYCoder
 */
@Data
public class RolePermsBO {

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 权限集合
     */
    private Set<String> perms;
}


