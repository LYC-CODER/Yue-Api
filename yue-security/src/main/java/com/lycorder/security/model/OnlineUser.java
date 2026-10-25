package com.lycorder.security.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 在线用户信息对象。
 * <p>
 * 在 Redis Token 模式下缓存当前会话用户的身份、部门、数据权限范围及角色信息，
 * 令牌解析时据此重建认证对象。
 *
 * @author LYCoder
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUser {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 数据权限范围
     * <p>定义用户可访问的数据范围，如全部、本部门或自定义范围</p>
     */
    private Integer dataScope;

    /**
     * 角色权限集合
     */
    private Set<String> roles;

}
