package com.lycorder.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lycorder.system.model.entity.SysDict;
import com.lycorder.system.model.query.DictQuery;
import com.lycorder.system.model.vo.DictPageVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典 访问层
 *
 * @author LYCoder
 */
@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {

    /**
     * 获取字典分页列表
     *
     * @param objectPage 分页参数
     * @param queryParams 查询参数
     * @return 字典分页列表
     */
    Page<DictPageVO> getDictPage(Page<Object> objectPage, DictQuery queryParams);
}




