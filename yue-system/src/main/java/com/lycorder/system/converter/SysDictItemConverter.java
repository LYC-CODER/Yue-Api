package com.lycorder.system.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lycorder.common.model.Option;
import com.lycorder.system.model.entity.SysDictItem;
import com.lycorder.system.model.form.DictItemForm;
import com.lycorder.system.model.vo.DictPageVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 字典项对象转换器
 *
 * @author Ray.Hao
 * @since 2022/6/8
 */
@Mapper(componentModel = "spring")
public interface SysDictItemConverter {

    Page<DictPageVO> toPageVo(Page<SysDictItem> page);

    DictItemForm toForm(SysDictItem entity);

    SysDictItem toEntity(DictItemForm formFata);

    Option<Long> toOption(SysDictItem dictItem);
    List<Option<Long>> toOption(List<SysDictItem> dictData);
}
