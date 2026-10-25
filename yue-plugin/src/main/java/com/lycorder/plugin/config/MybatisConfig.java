package com.lycorder.plugin.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;


import com.lycorder.plugin.mybatis.ApiDataPermissionHandler;
import com.lycorder.plugin.mybatis.ApiMetaObjectHandler;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Properties;

/**
 * MyBatis-Plus 配置类。
 * <p>
 * 统一装配数据权限、分页、字段自动填充和数据库类型识别等基础设施，
 * 并根据配置的数据库类型选择对应的分页方言。
 * </p>
 *
 * @author LYCoder
 */
@Configuration
@EnableTransactionManagement
public class MybatisConfig {

    /**
     * 数据库类型，默认 mysql，支持 mysql、postgres、postgresql
     */
    @Value("${app.db-type:mysql}")
    private String dbType;

    /**
     * 注册分页插件和数据权限插件。
     * <p>
     * 数据权限插件负责在 SQL 中追加数据范围条件；分页插件根据配置的数据库类型
     * 动态选择方言，未配置时默认使用 MySQL。
     * </p>
     *
     * @return MyBatis-Plus 拦截器链
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 数据权限
        interceptor.addInnerInterceptor(new DataPermissionInterceptor(new ApiDataPermissionHandler()));

        // 分页插件，根据配置动态选择数据库类型
        DbType mpDbType = DbType.MYSQL;
        String type = dbType == null ? "mysql" : dbType.toLowerCase();
        // 兼容 postgres 和 postgresql 两种写法，其余情况保持 MySQL 方言
        if ("postgres".equals(type) || "postgresql".equals(type)) {
            mpDbType = DbType.POSTGRE_SQL;
        }
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(mpDbType));

        return interceptor;
    }

    /**
     * 自动填充数据库创建人、创建时间、更新人、更新时间。
     * <p>
     * 当前处理器实际填充 createTime、updateTime。
     * </p>
     *
     * @return MyBatis-Plus 全局配置
     */
    @Bean
    public GlobalConfig globalConfig() {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(new ApiMetaObjectHandler());
        return globalConfig;
    }

    /**
     * 数据库类型自动识别。
     * <p>
     * 通过 JDBC 元数据获取数据库厂商名称，此处仅登记 MySQL 的映射关系。
     * </p>
     *
     * @return 数据库厂商标识提供者
     */
    @Bean
    public DatabaseIdProvider databaseIdProvider() {
        DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
        Properties properties = new Properties();
        properties.setProperty("MySQL", "mysql");
        databaseIdProvider.setProperties(properties);
        return databaseIdProvider;
    }
}