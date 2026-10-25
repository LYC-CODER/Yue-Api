package com.lycorder.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lycorder.system.model.entity.SysLog;
import com.lycorder.system.model.query.LogQuery;
import com.lycorder.system.model.vo.LogPageVO;

/**
* 操作日志业务接口，提供日志分页查询。
*
* @author LYCorder
* @description 针对表【sys_log(系统操作日志表)】的数据库操作Service
* @createDate 2026-01-23 19:26:46
*/
public interface SysLogService extends IService<SysLog> {

    /**
     * 分页查询系统操作日志。
     *
     * @param queryParams 日志查询参数
     * @return 日志分页数据
     */
    Page<LogPageVO> getLogPage(LogQuery queryParams);
}
