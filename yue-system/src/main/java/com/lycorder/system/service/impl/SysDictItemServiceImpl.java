package com.lycorder.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lycorder.system.converter.SysDictItemConverter;
import com.lycorder.system.mapper.SysDictItemMapper;
import com.lycorder.system.model.entity.SysDictItem;
import com.lycorder.system.model.form.DictItemForm;
import com.lycorder.system.model.query.DictItemQuery;
import com.lycorder.system.model.vo.DictItemOptionVO;
import com.lycorder.system.model.vo.DictItemPageVO;
import com.lycorder.system.service.SysDictItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;


/**
 * 字典项实现类
 *
 * @author LYCoder
 */
@Service
@RequiredArgsConstructor
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements SysDictItemService {

    private final SysDictItemConverter sysDictItemConverter;
    @Override
    public Page<DictItemPageVO> getDictItemPage(DictItemQuery queryParams) {
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        Page<DictItemPageVO> page = new Page<>(pageNum, pageSize);
        return this.baseMapper.getDictItemPage(page, queryParams);
    }

    @Override
    public List<DictItemOptionVO> getDictItems(String dictCode) {
        return this.list(
                        new LambdaQueryWrapper<SysDictItem>()
                                .eq(SysDictItem::getDictCode, dictCode)
                                .eq(SysDictItem::getStatus, 1)
                                .orderByAsc(SysDictItem::getSort)
                ).stream()
                .map(item -> {
                    DictItemOptionVO dictItemOptionVo = new DictItemOptionVO();
                    dictItemOptionVo.setLabel(item.getLabel());
                    dictItemOptionVo.setValue(item.getValue());
                    dictItemOptionVo.setTagType(item.getTagType());
                    return dictItemOptionVo;
                }).toList();
    }

    @Override
    public boolean saveDictItem(DictItemForm formData) {
        SysDictItem entity = sysDictItemConverter.toEntity(formData);
        return this.save(entity);
    }

    @Override
    public DictItemForm getDictItemForm(Long itemId) {
        SysDictItem entity = this.getById(itemId);
        return sysDictItemConverter.toForm(entity);
    }

    @Override
    public boolean updateDictItem(DictItemForm formData) {
        SysDictItem entity = sysDictItemConverter.toEntity(formData);
        return this.updateById(entity);
    }

    @Transactional
    @Override
    public void deleteDictItemByIds(String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();
        this.removeByIds(idList);
    }
}




