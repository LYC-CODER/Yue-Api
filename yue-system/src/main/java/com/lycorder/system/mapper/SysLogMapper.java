package com.lycorder.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lycorder.system.model.entity.SysLog;
import com.lycorder.system.model.query.LogQuery;
import com.lycorder.system.model.vo.LogPageVO;
import org.apache.ibatis.annotations.Mapper;

/**
* @author LYCorder
* @description 针对表【sys_log(系统操作日志表)】的数据库操作Mapper
* @createDate 2026-01-23 19:26:46
* @Entity com.lycorder.model.entity.SysLog
*/
@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {

    Page<LogPageVO> getLogPage(Page<Object> objectPage, LogQuery queryParams);
}




