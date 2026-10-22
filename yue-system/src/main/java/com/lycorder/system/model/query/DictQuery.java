package com.lycorder.system.model.query;

import com.lycorder.common.base.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author LYCoder
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description ="字典查询对象")
public class DictQuery extends BaseQuery {

    @Schema(description="关键字(字典名称)")
    private String keywords;

}
