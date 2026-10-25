package com.lycorder.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lycorder.system.model.entity.SysConfig;
import com.lycorder.system.model.form.ConfigForm;
import com.lycorder.system.model.query.ConfigQuery;
import com.lycorder.system.model.vo.ConfigPageVO;

/**
 * 系统配置业务接口。
 *
 * @author LYCoder
 */
public interface SysConfigService extends IService<SysConfig> {

    /** 配置分页列表 */
    Page<ConfigPageVO> getConfigPage(ConfigQuery queryParams);

    /** 获取配置表单 */
    ConfigForm getConfigForm(Long id);

    /** 新增配置 */
    boolean saveConfig(ConfigForm formData);

    /** 修改配置 */
    boolean updateConfig(Long id, ConfigForm formData);

    /** 批量删除配置 */
    boolean deleteConfigs(String ids);

    /** 根据 key 获取配置值 */
    String getConfigValue(String configKey);
}
