package com.lycorder.system.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lycorder.common.model.Option;
import com.lycorder.system.model.entity.SysDict;
import com.lycorder.system.model.form.DictForm;
import com.lycorder.system.model.query.DictQuery;
import com.lycorder.system.model.vo.DictPageVO;
import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 字典业务接口
 *
 * @author LYCoder
 */
public interface SysDictService extends IService<SysDict> {

    /**
     * 获取字典分页列表
     *
     * @param queryParams 查询参数
     * @return 字典分页列表
     */
    Page<DictPageVO> getDictPage(DictQuery queryParams);

    /**
     * 获取字典项列表
     *
     * @return 字典项列表
     */
    List<Option<String>> getDictList();

    /**
     * 获取字典表单
     *
     * @param id 字典ID
     * @return 字典表单
     */
    DictForm getDictForm(Long id);

    /**
     * 保存字典
     *
     * @param dictForm 字典表单
     * @return 保存结果
     */
    boolean saveDict(@Valid DictForm dictForm);

    /**
     * 更新字典
     *
     * @param id         字典ID
     * @param dictForm 字典表单
     * @return 更新结果
     */
    boolean updateDict(Long id, DictForm dictForm);

    /**
     * 获取字典项列表
     *
     * @param ids 字典ID列表
     * @return 字典项列表
     */
    List<String> getDictCodesByIds(List<String> ids);

    /**
     * 删除字典
     *
     * @param ids 字典ID列表
     */
    void deleteDictByIds(List<String> ids);
}
