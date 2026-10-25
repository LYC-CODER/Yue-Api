package com.lycorder.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lycorder.system.model.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置访问层。
 *
 * @author LYCoder
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
}
