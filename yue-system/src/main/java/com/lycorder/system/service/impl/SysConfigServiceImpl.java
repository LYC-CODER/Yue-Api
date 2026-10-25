package com.lycorder.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lycorder.core.exception.BusinessException;
import com.lycorder.security.util.SecurityUtils;
import com.lycorder.system.mapper.SysConfigMapper;
import com.lycorder.system.model.entity.SysConfig;
import com.lycorder.system.model.form.ConfigForm;
import com.lycorder.system.model.query.ConfigQuery;
import com.lycorder.system.model.vo.ConfigPageVO;
import com.lycorder.system.service.SysConfigService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 系统配置业务实现。
 *
 * @author LYCoder
 */
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig>
        implements SysConfigService {

    @Override
    public Page<ConfigPageVO> getConfigPage(ConfigQuery queryParams) {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(queryParams.getKeywords())) {
            String keywords = queryParams.getKeywords().trim();
            wrapper.and(w -> w
                    .like(SysConfig::getConfigName, keywords)
                    .or()
                    .like(SysConfig::getConfigKey, keywords));
        }
        wrapper.orderByDesc(SysConfig::getCreateTime);

        Page<SysConfig> page = this.page(
                new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
                wrapper
        );

        Page<ConfigPageVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toPageVO).toList());
        return result;
    }

    @Override
    public ConfigForm getConfigForm(Long id) {
        SysConfig entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("系统配置不存在");
        }
        ConfigForm form = new ConfigForm();
        form.setId(entity.getId());
        form.setConfigName(entity.getConfigName());
        form.setConfigKey(entity.getConfigKey());
        form.setConfigValue(entity.getConfigValue());
        form.setRemark(entity.getRemark());
        return form;
    }

    @Override
    public boolean saveConfig(ConfigForm formData) {
        String configKey = formData.getConfigKey().trim();
        checkKeyUnique(configKey, null);

        SysConfig entity = new SysConfig();
        entity.setConfigName(formData.getConfigName().trim());
        entity.setConfigKey(configKey);
        entity.setConfigValue(formData.getConfigValue());
        entity.setRemark(formData.getRemark());
        entity.setCreateBy(SecurityUtils.getUserId());
        return this.save(entity);
    }

    @Override
    public boolean updateConfig(Long id, ConfigForm formData) {
        SysConfig old = this.getById(id);
        if (old == null) {
            throw new BusinessException("系统配置不存在");
        }

        String configKey = formData.getConfigKey().trim();
        if (!configKey.equals(old.getConfigKey())) {
            checkKeyUnique(configKey, id);
        }

        SysConfig entity = new SysConfig();
        entity.setId(id);
        entity.setConfigName(formData.getConfigName().trim());
        entity.setConfigKey(configKey);
        entity.setConfigValue(formData.getConfigValue());
        entity.setRemark(formData.getRemark());
        entity.setUpdateBy(SecurityUtils.getUserId());
        return this.updateById(entity);
    }

    @Override
    public boolean deleteConfigs(String ids) {
        if (StrUtil.isBlank(ids)) {
            throw new BusinessException("删除的系统配置数据为空");
        }
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .map(Long::parseLong)
                .toList();
        return this.removeByIds(idList);
    }

    @Override
    public String getConfigValue(String configKey) {
        if (StrUtil.isBlank(configKey)) {
            return null;
        }
        SysConfig config = this.getOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey)
                .last("LIMIT 1"));
        return config == null ? null : config.getConfigValue();
    }

    /**
     * 校验配置 key 唯一。
     *
     * @param configKey 配置 key
     * @param excludeId 更新时排除自身 ID
     */
    private void checkKeyUnique(String configKey, Long excludeId) {
        long count = this.count(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, configKey)
                .ne(excludeId != null, SysConfig::getId, excludeId));
        if (count > 0) {
            throw new BusinessException("配置key已存在");
        }
    }

    /**
     * 实体转换为分页 VO。
     *
     * @param entity 配置实体
     * @return 分页 VO
     */
    private ConfigPageVO toPageVO(SysConfig entity) {
        ConfigPageVO vo = new ConfigPageVO();
        vo.setId(entity.getId());
        vo.setConfigName(entity.getConfigName());
        vo.setConfigKey(entity.getConfigKey());
        vo.setConfigValue(entity.getConfigValue());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
