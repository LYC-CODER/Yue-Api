package com.lycorder.system.listener;

import com.lycorder.core.event.LogEvent;
import com.lycorder.system.model.entity.SysLog;
import com.lycorder.system.service.SysLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 操作日志事件监听器
 * <p>
 * 监听 yue-security 日志切面发布的 {@link LogEvent}，转换为 {@link SysLog} 实体落库，
 * 解除框架层（core/security）对业务层（system）的依赖。
 * </p>
 *
 * @author LYCoder
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogEventListener {

    private final SysLogService sysLogService;

    /**
     * 处理日志事件，保存日志到数据库
     *
     * @param event 日志事件
     */
    @EventListener
    public void onLogEvent(LogEvent event) {
        try {
            SysLog sysLog = new SysLog();
            BeanUtils.copyProperties(event, sysLog);
            sysLogService.save(sysLog);
        } catch (Exception e) {
            // 日志持久化失败不影响主流程
            log.error("保存操作日志失败: {}", e.getMessage());
        }
    }
}
