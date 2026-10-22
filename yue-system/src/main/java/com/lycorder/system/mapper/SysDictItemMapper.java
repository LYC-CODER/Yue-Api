package com.lycorder.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lycorder.system.model.entity.SysDictItem;
import com.lycorder.system.model.query.DictItemQuery;
import com.lycorder.system.model.vo.DictItemPageVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典项映射层
 *
 * @author LYCoder
 */
@Mapper
public interface SysDictItemMapper extends BaseMapper<SysDictItem> {

    Page<DictItemPageVO> getDictItemPage(Page<DictItemPageVO> page, DictItemQuery queryParams);
}




