package com.lycorder.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lycorder.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置实体。
 *
 * <p>对应 sys_config 表，保存可动态调整的系统参数。</p>
 *
 * @author LYCoder
 */
@EqualsAndHashCode(callSuper = false)
@TableName("sys_config")
@Data
public class SysConfig extends BaseEntity {

    /** 配置名称 */
    private String configName;

    /** 配置 key，全局唯一 */
    private String configKey;

    /** 配置值 */
    private String configValue;

    /** 备注 */
    private String remark;

    /** 创建人 ID */
    private Long createBy;

    /** 更新人 ID */
    private Long updateBy;

    /** 逻辑删除标识（0-未删除，1-已删除） */
    @TableLogic
    private Integer isDeleted;
}
