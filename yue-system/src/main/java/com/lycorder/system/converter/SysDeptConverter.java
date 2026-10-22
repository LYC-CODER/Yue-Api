package com.lycorder.system.converter;

import com.lycorder.system.model.entity.SysDept;
import com.lycorder.system.model.form.DeptForm;
import com.lycorder.system.model.vo.DeptVO;
import org.mapstruct.Mapper;

/**
 * 部门对象转换器
 *
 * @author LYCoder
 */
@Mapper(componentModel = "spring")
public interface SysDeptConverter {

    DeptForm toForm(SysDept entity);
    
    DeptVO toVo(SysDept entity);

    SysDept toEntity(DeptForm deptForm);

}