package com.lycorder.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lycorder.system.model.entity.SysDictItem;
import com.lycorder.system.model.form.DictItemForm;
import com.lycorder.system.model.query.DictItemQuery;
import com.lycorder.system.model.vo.DictItemOptionVO;
import com.lycorder.system.model.vo.DictItemPageVO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 字典项接口
 *
 * @author LYCoder
 */
public interface SysDictItemService extends IService<SysDictItem> {

    /**
     * 字典项列表
     *
     * @param queryParams 查询参数
     * @return 字典项列表
     */
    Page<DictItemPageVO> getDictItemPage(DictItemQuery queryParams);

    /**
     * 字典项选项列表
     *
     * @return 字典项选项列表
     */
    List<DictItemOptionVO> getDictItems(String dictCode);

    /**
     * 新增字典项
     *
     * @param formData 字典项表单数据
     * @return 字典项ID
     */
    boolean saveDictItem(@Valid DictItemForm formData);

    /**
     * 获取字典项表单数据
     *
     * @param itemId 字典项ID
     * @return 字典项表单数据
     */
    DictItemForm getDictItemForm(Long itemId);

    /**
     * 更新字典项
     *
     * @param formData 字典项表单数据
     * @return 字典项ID
     */
    boolean updateDictItem(DictItemForm formData);

    /**
     * 删除字典项
     *
     * @param ids 字典项ID，多个用英文逗号(,)分割
     */
    void deleteDictItemByIds(String ids);
}
