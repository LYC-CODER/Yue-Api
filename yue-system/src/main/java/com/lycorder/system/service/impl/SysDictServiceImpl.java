package com.lycorder.system.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lycorder.common.model.Option;
import com.lycorder.core.exception.BusinessException;
import com.lycorder.system.converter.SysDictConverter;
import com.lycorder.system.mapper.SysDictMapper;
import com.lycorder.system.model.entity.SysDict;
import com.lycorder.system.model.entity.SysDictItem;
import com.lycorder.system.model.form.DictForm;
import com.lycorder.system.model.query.DictQuery;
import com.lycorder.system.model.vo.DictPageVO;
import com.lycorder.system.service.SysDictItemService;
import com.lycorder.system.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 字典业务实现类
 *
 * @author haoxr
 * @since 2022/10/12
 */
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {


    private final SysDictItemService sysDictItemService;
    private final SysDictConverter sysDictConverter;

    /**
     * 字典分页列表
     *
     * @param queryParams 分页查询对象
     */
    @Override
    public Page<DictPageVO> getDictPage(DictQuery queryParams) {

        // 查询参数
        int pageNum = queryParams.getPageNum();
        int pageSize = queryParams.getPageSize();

        // 查询数据
        return this.baseMapper.getDictPage(new Page<>(pageNum, pageSize), queryParams);
    }



    /**
     * 获取字典列表
     *
     * @return 字典列表
     */
    @Override
    public List<Option<String>> getDictList() {
        return this.list(new LambdaQueryWrapper<SysDict>().eq(SysDict::getStatus, 1))
                .stream()
                .map(item -> new Option<>(item.getDictCode(), item.getName()))
                .toList();
    }

    /**
     * 获取字典表单详情
     *
     * @param id 字典ID
     */
    @Override
    public DictForm getDictForm(Long id) {

        // 获取字典
        SysDict entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("字典不存在");
        }
        return sysDictConverter.toForm(entity);
    }

    @Override
    public boolean saveDict(DictForm dictForm) {

        // 保存字典
        SysDict entity = sysDictConverter.toEntity(dictForm);

        // 校验 code 是否唯一
        String dictCode = entity.getDictCode();

        long count = this.count(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictCode, dictCode)
        );

        Assert.isTrue(count == 0, "字典编码已存在");

        return this.save(entity);
    }

    @Override
    public boolean updateDict(Long id, DictForm dictForm) {

        // 获取字典
        SysDict entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("字典不存在");
        }
        // 校验 code 是否唯一
        String dictCode = dictForm.getDictCode();
        if (!entity.getDictCode().equals(dictCode)) {
            long count = this.count(new LambdaQueryWrapper<SysDict>()
                    .eq(SysDict::getDictCode, dictCode)
            );
            Assert.isTrue(count == 0, "字典编码已存在");
        }
        // 更新字典
        SysDict dict = sysDictConverter.toEntity(dictForm);
        dict.setId(id);
        boolean result = this.updateById(dict);
        if (result) {
            // 更新字典数据
            List<SysDictItem> dictItemList = sysDictItemService.list(
                    new LambdaQueryWrapper<SysDictItem>()
                            .eq(SysDictItem::getDictCode, entity.getDictCode())
                            .select(SysDictItem::getId)
            );
            if (!dictItemList.isEmpty()){
                List<Long> dictItemIds = dictItemList.stream().map(SysDictItem::getId).toList();
                SysDictItem dictItem = new SysDictItem();
                dictItem.setDictCode(dict.getDictCode());
                sysDictItemService.update(dictItem,
                        new LambdaQueryWrapper<SysDictItem>()
                                .in(SysDictItem::getId, dictItemIds)
                );
            }
        }
        return result;
    }

    @Override
    public List<String> getDictCodesByIds(List<String> ids) {
        List<SysDict> dictList = this.listByIds(ids);
        return dictList.stream().map(SysDict::getDictCode).toList();
    }

    @Transactional
    @Override
    public void deleteDictByIds(List<String> ids) {
        // 删除字典
        this.removeByIds(ids);

        // 删除字典项
        List<SysDict> list = this.listByIds(ids);
        if (!list.isEmpty()) {
            List<String> dictCodes = list.stream().map(SysDict::getDictCode).toList();
            sysDictItemService.remove(new LambdaQueryWrapper<SysDictItem>()
                    .in(SysDictItem::getDictCode, dictCodes)
            );
        }
    }
}




