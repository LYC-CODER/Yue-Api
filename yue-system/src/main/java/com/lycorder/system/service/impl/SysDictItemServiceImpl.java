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
 * 字典项业务实现，负责字典项分页、选项查询及维护。
 *
 * @author LYCoder
 */
@Service
@RequiredArgsConstructor
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements SysDictItemService {

    private final SysDictItemConverter sysDictItemConverter;
    /**
     * 分页查询字典项。
     *
     * @param queryParams 字典项查询参数
     * @return 字典项分页数据
     */
    @Override
    public Page<DictItemPageVO> getDictItemPage(DictItemQuery queryParams) {
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();
        Page<DictItemPageVO> page = new Page<>(pageNum, pageSize);
        return this.baseMapper.getDictItemPage(page, queryParams);
    }

    /**
     * 获取指定字典下启用状态的字典项选项。
     *
     * @param dictCode 字典编码
     * @return 字典项选项列表
     */
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

    /**
     * 新增字典项。
     *
     * @param formData 字典项表单数据
     * @return 是否保存成功
     */
    @Override
    public boolean saveDictItem(DictItemForm formData) {
        SysDictItem entity = sysDictItemConverter.toEntity(formData);
        return this.save(entity);
    }

    /**
     * 获取字典项编辑表单数据。
     *
     * @param itemId 字典项ID
     * @return 字典项表单数据
     */
    @Override
    public DictItemForm getDictItemForm(Long itemId) {
        SysDictItem entity = this.getById(itemId);
        return sysDictItemConverter.toForm(entity);
    }

    /**
     * 修改字典项。
     *
     * @param formData 字典项表单数据
     * @return 是否修改成功
     */
    @Override
    public boolean updateDictItem(DictItemForm formData) {
        SysDictItem entity = sysDictItemConverter.toEntity(formData);
        return this.updateById(entity);
    }

    /**
     * 批量删除字典项。
     *
     * @param ids 字典项ID字符串，多个以英文逗号分隔
     */
    @Transactional
    @Override
    public void deleteDictItemByIds(String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();
        this.removeByIds(idList);
    }
}




