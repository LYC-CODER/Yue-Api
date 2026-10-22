package com.lycorder.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lycorder.common.model.Option;
import com.lycorder.system.model.entity.SysDept;
import com.lycorder.system.model.form.DeptForm;
import com.lycorder.system.model.query.DeptQuery;
import com.lycorder.system.model.vo.DeptVO;
import jakarta.validation.Valid;

import java.util.List;


/**
* @author LYCorder
* @description 针对表【sys_dept(部门管理表)】的数据库操作Service
* @createDate 2026-01-23 19:26:46
*/
public interface SysDeptService extends IService<SysDept> {

    /**
     * 部门列表
     *
     * @param queryParams 查询参数
     * @return 部门列表
     */
    List<DeptVO> getDeptList(DeptQuery queryParams);

    /**
     * 部门选项列表
     *
     * @return 部门选项列表
     */
    List<Option<Long>> listDeptOptions();

    /**
     * 新增部门
     *
     * @param formData 部门表单数据
     * @return 部门ID
     */
    Long saveDept(@Valid DeptForm formData);

    /**
     * 获取部门表单数据
     *
     * @param deptId 部门ID
     * @return 部门表单数据
     */
    DeptForm getDeptForm(Long deptId);

    /**
     * 更新部门
     *
     * @param deptId 部门ID
     * @param formData 部门表单数据
     * @return 部门ID
     */
    Long updateDept(Long deptId, @Valid DeptForm formData);


    /**
     * 删除部门
     *
     * @param ids 部门ID，多个用英文逗号(,)分割
     * @return 是否成功
     */
    boolean deleteByIds(String ids);
}
