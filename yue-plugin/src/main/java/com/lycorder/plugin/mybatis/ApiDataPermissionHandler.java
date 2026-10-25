package com.lycorder.plugin.mybatis;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;

import com.lycorder.common.annotation.DataPermission;
import com.lycorder.common.base.IBaseEnum;
import com.lycorder.common.enums.DataScopeEnum;
import com.lycorder.security.util.SecurityUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;

import java.lang.reflect.Method;

/**
 * 数据权限处理器。
 * <p>
 * 在 MyBatis-Plus 执行 SQL 前，根据当前登录用户的数据范围动态追加 WHERE 条件；
 * 仅对标注了 {@link DataPermission} 的 Mapper 方法生效。
 * </p>
 *
 * @author LYCoder
 */
@Slf4j
public class ApiDataPermissionHandler implements DataPermissionHandler {

    /**
     * 获取数据权限的 SQL 片段。
     * <p>
     * 未登录、超级管理员或数据范围为全部时不限制；Mapper 方法未标注
     * {@link DataPermission} 时也直接返回原查询条件。
     * </p>
     *
     * @param where 查询条件
     * @param mappedStatementId mapper接口方法的全路径
     * @return sql片段
     * @throws ClassNotFoundException 当 mappedStatementId 对应的 Mapper 接口无法加载时抛出
     */
    @Override
    @SneakyThrows
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        // 如果是未登录，或者是定时任务执行的SQL，或者是超级管理员，直接返回
        if(SecurityUtils.getUserId() == null || SecurityUtils.isRoot()){
            return where;
        }
        // 获取当前用户的数据权限
        Integer dataScope = SecurityUtils.getDataScope();
        DataScopeEnum dataScopeEnum = IBaseEnum.getEnumByValue(dataScope, DataScopeEnum.class);
        // 如果是全部数据权限，直接返回
        if (DataScopeEnum.ALL.equals(dataScopeEnum)) {
            return where;
        }
        // 获取当前执行的接口类
        Class<?> clazz = Class.forName(mappedStatementId.substring(0, mappedStatementId.lastIndexOf(StringPool.DOT)));
        // 获取当前执行的方法名称
        String methodName = mappedStatementId.substring(mappedStatementId.lastIndexOf(StringPool.DOT) + 1);
        // 获取当前执行的接口类里所有的方法
        Method[] methods = clazz.getDeclaredMethods();
        // 仅按方法名匹配；数据权限场景下 Mapper 方法名应保持唯一，避免重载导致注解读取错误
        for (Method method : methods) {
            //找到当前执行的方法
            if (method.getName().equals(methodName)) {
                DataPermission annotation = method.getAnnotation(DataPermission.class);
                // 判断当前执行的方法是否有权限注解，如果没有注解直接返回
                if (annotation == null ) {
                    return where;
                }
                return dataScopeFilter(annotation.deptAlias(), annotation.deptIdColumnName(), annotation.userAlias(), annotation.userIdColumnName(), dataScopeEnum,where);
            }
        }
        return where;
    }

    /**
     * 构建数据范围过滤条件。
     * <p>
     * 根据数据范围拼接部门或用户字段条件，并与原有 WHERE 条件通过 AND 组合。
     * </p>
     *
     * @param deptAlias 部门表别名，可为空
     * @param deptIdColumnName 部门 ID 字段名
     * @param userAlias 用户表别名，可为空
     * @param userIdColumnName 用户 ID 字段名
     * @param dataScopeEnum 数据范围枚举
     * @param where 当前查询条件
     * @return 构建后查询条件
     * @throws net.sf.jsqlparser.JSQLParserException 条件表达式解析失败时抛出
     */
    @SneakyThrows
    public static Expression dataScopeFilter(String deptAlias, String deptIdColumnName, String userAlias, String userIdColumnName,DataScopeEnum dataScopeEnum, Expression where) {

        // 获取部门和用户的别名
        String deptColumnName = StrUtil.isNotBlank(deptAlias) ? (deptAlias + StringPool.DOT + deptIdColumnName) : deptIdColumnName;
        String userColumnName = StrUtil.isNotBlank(userAlias) ? (userAlias + StringPool.DOT + userIdColumnName) : userIdColumnName;

        Long deptId, userId;
        String appendSqlStr;
        switch (dataScopeEnum) {
            case ALL:
                return where;
            case DEPT:
                deptId = SecurityUtils.getDeptId();
                appendSqlStr = deptColumnName + StringPool.EQUALS + deptId;
                break;
            case SELF:
                userId = SecurityUtils.getUserId();
                appendSqlStr = userColumnName + StringPool.EQUALS + userId;
                break;
            // 默认按本部门及子部门过滤；tree_path 保存部门祖先路径，FIND_IN_SET 为 MySQL 语法
            default:
                deptId = SecurityUtils.getDeptId();
                appendSqlStr = deptColumnName + " IN ( SELECT id FROM sys_dept WHERE id = " + deptId + " OR FIND_IN_SET( " + deptId + " , tree_path ) )";
                break;
        }

        if (StrUtil.isBlank(appendSqlStr)) {
            return where;
        }

        // 把拼接好的条件解析为表达式，再与原 WHERE 条件做 AND 组合
        Expression appendExpression = CCJSqlParserUtil.parseCondExpression(appendSqlStr);

        // 原 SQL 没有 WHERE 条件时，权限条件直接作为查询条件
        if (where == null) {
            return appendExpression;
        }

        return new AndExpression(where, appendExpression);
    }


}

