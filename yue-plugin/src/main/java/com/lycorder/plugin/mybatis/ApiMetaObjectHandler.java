package com.lycorder.plugin.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * mybatis-plus 字段自动填充。
 * <p>
 * 新增时填充创建时间、更新时间，更新时仅刷新更新时间；
 * strict 系列填充只在字段为 null 时写入，不会覆盖业务已赋的值。
 * </p>
 *
 * @author LYCoder
 */
@Component
@RequiredArgsConstructor
public class ApiMetaObjectHandler implements MetaObjectHandler {

    /**
     * 新增填充创建时间、更新时间
     *
     * @param metaObject 元数据
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 新增时初始化创建时间，updateTime 同步写入
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
    }

    /**
     * 更新填充更新时间
     *
     * @param metaObject 元数据
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时只刷新更新时间，创建时间保持不变
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
    }

}
