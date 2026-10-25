package com.lycorder.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lycorder.system.mapper.SysUserNoticeMapper;
import com.lycorder.system.model.entity.SysUserNotice;
import com.lycorder.system.model.query.NoticeQuery;
import com.lycorder.system.model.vo.UserNoticePageVO;
import com.lycorder.system.service.SysUserNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户通知状态业务实现。
 *
 * @author LYCoder
 */
@Service
@RequiredArgsConstructor
public class SysUserNoticeServiceImpl extends ServiceImpl<SysUserNoticeMapper, SysUserNotice>
        implements SysUserNoticeService {

    @Override
    public Page<UserNoticePageVO> getMyNoticePage(Page<UserNoticePageVO> page, NoticeQuery queryParams) {
        return this.baseMapper.getMyNoticePage(page, queryParams);
    }

    @Override
    public void markRead(Long noticeId, Long userId) {
        this.update(new LambdaUpdateWrapper<SysUserNotice>()
                .eq(SysUserNotice::getNoticeId, noticeId)
                .eq(SysUserNotice::getUserId, userId)
                .eq(SysUserNotice::getIsRead, 0)
                .set(SysUserNotice::getIsRead, 1)
                .set(SysUserNotice::getReadTime, LocalDateTime.now()));
    }

    @Override
    public void readAll(Long userId) {
        this.update(new LambdaUpdateWrapper<SysUserNotice>()
                .eq(SysUserNotice::getUserId, userId)
                .eq(SysUserNotice::getIsRead, 0)
                .set(SysUserNotice::getIsRead, 1)
                .set(SysUserNotice::getReadTime, LocalDateTime.now()));
    }

    @Override
    public long countUnread(Long userId) {
        return this.count(new LambdaQueryWrapper<SysUserNotice>()
                .eq(SysUserNotice::getUserId, userId)
                .eq(SysUserNotice::getIsRead, 0));
    }

    @Override
    public boolean exists(Long noticeId, Long userId) {
        return this.count(new LambdaQueryWrapper<SysUserNotice>()
                .eq(SysUserNotice::getNoticeId, noticeId)
                .eq(SysUserNotice::getUserId, userId)) > 0;
    }

    @Override
    public void removeByNoticeIds(List<Long> noticeIds) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return;
        }
        this.remove(new LambdaQueryWrapper<SysUserNotice>()
                .in(SysUserNotice::getNoticeId, noticeIds));
    }
}
