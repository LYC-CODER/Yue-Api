package com.lycorder.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lycorder.system.mapper.SysLogMapper;
import com.lycorder.system.model.entity.SysLog;
import com.lycorder.system.model.query.LogQuery;
import com.lycorder.system.model.vo.LogPageVO;
import com.lycorder.system.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
* @author LYCorder
* @description 针对表【sys_log(系统操作日志表)】的数据库操作Service实现
* @createDate 2026-01-23 19:26:46
*/
@Service
@RequiredArgsConstructor
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog>
    implements SysLogService {

    @Override
    public Page<LogPageVO> getLogPage(LogQuery queryParams) {
        return this.baseMapper.getLogPage(new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
                queryParams);
    }
}




