package com.lycorder.system.listener;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lycorder.common.constant.SystemConstants;
import com.lycorder.common.enums.StatusEnum;
import com.lycorder.core.result.ExcelResult;
import com.lycorder.system.converter.SysUserConverter;
import com.lycorder.system.enums.DictCodeEnum;
import com.lycorder.system.model.dto.UserImportDTO;
import com.lycorder.system.model.entity.*;
import com.lycorder.system.service.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户导入监听器
 * <p>
 * <a href="https://easyexcel.opensource.alibaba.com/docs/current/quickstart/read#%E6%9C%80%E7%AE%80%E5%8D%95%E7%9A%84%E8%AF%BB%E7%9A%84%E7%9B%91%E5%90%AC%E5%99%A8">最简单的读的监听器</a>
 *
 * @author Ray
 * @since 2022/4/10
 */
@Slf4j
public class UserImportListener extends AnalysisEventListener<UserImportDTO> {

    /**
     * Excel 导入结果
     */
    @Getter
    private final ExcelResult excelResult;

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final SysUserConverter sysUserConverter;
    private final SysUserRoleService sysUserRoleService;

    private final List<SysRole> roleList;
    private final List<SysDept> deptList;
    private final List<SysDictItem> genderList;

    /**
     * 当前行
     */
    private Integer currentRow = 1;

    /**
     * 构造方法
     * <p>在构造方法中给需要查询的内容查询好，尽量避免每条数据查询一次</p>
     */
    public UserImportListener() {
        this.sysUserService = SpringUtil.getBean(SysUserService.class);
        this.passwordEncoder = SpringUtil.getBean(PasswordEncoder.class);
        this.sysUserRoleService = SpringUtil.getBean(SysUserRoleService.class);
        this.sysUserConverter = SpringUtil.getBean(SysUserConverter.class);
        this.roleList = SpringUtil.getBean(SysRoleService.class)
                .list(new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, StatusEnum.ENABLE.getValue())
                        .select(SysRole::getId, SysRole::getCode));
        this.deptList = SpringUtil.getBean(SysDeptService.class)
                .list(new LambdaQueryWrapper<SysDept>().select(SysDept::getId, SysDept::getCode));
        this.genderList = SpringUtil.getBean(SysDictItemService.class)
                .list(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictCode, DictCodeEnum.GENDER.getValue()));
        this.excelResult = new ExcelResult();
    }

    /**
     * 每一条数据解析都会来调用
     * <p>
     * 1. 数据校验；全字段校验
     * 2. 数据持久化；
     *
     * @param userImportDto 一行数据，类似于 {@link AnalysisContext#readRowHolder()}
     */
    @Override
    public void invoke(UserImportDTO userImportDto, AnalysisContext analysisContext) {
        log.info("解析到一条用户数据:{}", JSONUtil.toJsonStr(userImportDto));

        boolean validation = true;
        String errorMsg = "第" + currentRow + "行数据校验失败：";
        String username = userImportDto.getUsername();
        if (StrUtil.isBlank(username)) {
            errorMsg += "用户名为空；";
            validation = false;
        } else {
            long count = sysUserService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
            if (count > 0) {
                errorMsg += "用户名已存在；";
                validation = false;
            }
        }

        String nickname = userImportDto.getNickname();
        if (StrUtil.isBlank(nickname)) {
            errorMsg += "用户昵称为空；";
            validation = false;
        }

        String mobile = userImportDto.getMobile();
        if (StrUtil.isBlank(mobile)) {
            errorMsg += "手机号码为空；";
            validation = false;
        } else {
            if (!Validator.isMobile(mobile)) {
                errorMsg += "手机号码不正确；";
                validation = false;
            }
        }

        // 全部字段校验通过后再落库，避免无效数据进入数据库
        if (validation) {
            try {
                SysUser entity = sysUserConverter.toEntity(userImportDto);
                entity.setPassword(passwordEncoder.encode(SystemConstants.DEFAULT_PASSWORD));
                String genderLabel = userImportDto.getGenderLabel();
                entity.setGender(getGenderValue(genderLabel));
                String roleCodes = userImportDto.getRoleCodes();
                List<Long> roleIds = getRoleIds(roleCodes);
                String deptCode = userImportDto.getDeptCode();
                entity.setDeptId(getDeptId(deptCode));

                boolean saveResult = sysUserService.save(entity);
                if (saveResult) {
                    excelResult.setValidCount(excelResult.getValidCount() + 1);
                    if (CollectionUtil.isNotEmpty(roleIds)) {
                        List<SysUserRole> userRoles = roleIds.stream()
                                .map(roleId -> new SysUserRole(entity.getId(), roleId))
                                .collect(Collectors.toList());
                        sysUserRoleService.saveBatch(userRoles);
                    }
                } else {
                    excelResult.setInvalidCount(excelResult.getInvalidCount() + 1);
                    errorMsg += "第" + currentRow + "行数据保存失败；";
                    excelResult.getMessageList().add(errorMsg);
                }
            // 并发导入或 Excel 内重复用户名可能触发唯一键冲突，按行记录错误
            } catch (DuplicateKeyException e) {
                excelResult.setInvalidCount(excelResult.getInvalidCount() + 1);
                String duplicateErrorMsg = "第" + currentRow + "行数据保存失败：用户名 " + username + " 已存在（可能是 Excel 中存在重复用户名）";
                excelResult.getMessageList().add(duplicateErrorMsg);
                log.warn(duplicateErrorMsg, e);
            } catch (Exception e) {
                excelResult.setInvalidCount(excelResult.getInvalidCount() + 1);
                errorMsg += "第" + currentRow + "行数据保存异常：" + e.getMessage();
                excelResult.getMessageList().add(errorMsg);
                log.error("第{}行数据保存异常", currentRow, e);
            }
        } else {
            excelResult.setInvalidCount(excelResult.getInvalidCount() + 1);
            excelResult.getMessageList().add(errorMsg);
        }
        currentRow++;
    }


    /**
     * 根据角色编码获取角色ID
     *
     * @param roleCodes 角色编码 逗号分隔
     * @return 角色ID集合
     */
    private List<Long> getRoleIds(String roleCodes) {
        if (StrUtil.isNotBlank(roleCodes)) {
            String[] split = roleCodes.split(",");
            if (split.length > 0) {
                List<Long> roleIds = new ArrayList<>();
                for (String roleCode : split) {
                    this.roleList.stream().filter(r -> r.getCode().equals(roleCode))
                            .findFirst().ifPresent(role -> roleIds.add(role.getId()));
                }
                return roleIds.stream().distinct().toList();
            }
        }
        return Collections.emptyList();
    }

    /**
     * 根据部门编码获取部门ID
     *
     * @param deptCode 部门编码
     * @return 部门ID
     */
    private Long getDeptId(String deptCode) {
        if (StrUtil.isNotBlank(deptCode)) {
            return this.deptList.stream().filter(r -> r.getCode().equals(deptCode))
                    .findFirst().map(SysDept::getId).orElse(null);
        }
        return null;
    }

    /**
     * 根据性别标签获取性别值
     *
     * @param genderLabel 性别标签
     * @return 性别值
     */
    private Integer getGenderValue(String genderLabel) {
        if (StrUtil.isNotBlank(genderLabel)) {
            return this.genderList.stream()
                    .filter(r -> r.getLabel().equals(genderLabel))
                    .findFirst()
                    .map(SysDictItem::getValue)
                    .map(Convert::toInt)
                    .orElse(null);
        }
        return null;
    }

    /**
     * 所有数据解析完成会来调用
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("所有数据解析完成！");
    }

}
