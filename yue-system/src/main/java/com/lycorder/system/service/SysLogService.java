package com.lycorder.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lycorder.system.model.entity.SysLog;
import com.lycorder.system.model.query.LogQuery;
import com.lycorder.system.model.vo.LogPageVO;

/**
* @author LYCorder
* @description 针对表【sys_log(系统操作日志表)】的数据库操作Service
* @createDate 2026-01-23 19:26:46
*/
public interface SysLogService extends IService<SysLog> {

    Page<LogPageVO> getLogPage(LogQuery queryParams);
}
