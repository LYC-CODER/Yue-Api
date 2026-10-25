package com.lycorder.system.model.query;

import com.lycorder.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置查询参数。
 *
 * @author LYCoder
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "系统配置查询对象")
public class ConfigQuery extends BaseQuery {

    @Schema(description = "关键字（配置名称/配置key）")
    private String keywords;
}
