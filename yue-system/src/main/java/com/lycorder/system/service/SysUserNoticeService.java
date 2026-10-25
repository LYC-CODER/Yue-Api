package com.lycorder.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lycorder.system.model.entity.SysUserNotice;
import com.lycorder.system.model.query.NoticeQuery;
import com.lycorder.system.model.vo.UserNoticePageVO;

import java.util.List;

/**
 * 用户通知状态业务接口。
 *
 * @author LYCoder
 */
public interface SysUserNoticeService extends IService<SysUserNotice> {

    /** 我的通知分页 */
    Page<UserNoticePageVO> getMyNoticePage(Page<UserNoticePageVO> page, NoticeQuery queryParams);

    /** 标记单条通知已读 */
    void markRead(Long noticeId, Long userId);

    /** 当前用户全部已读 */
    void readAll(Long userId);

    /** 当前用户未读数量 */
    long countUnread(Long userId);

    /** 判断当前用户是否有该通知 */
    boolean exists(Long noticeId, Long userId);

    /** 删除公告时清理接收状态 */
    void removeByNoticeIds(List<Long> noticeIds);
}
