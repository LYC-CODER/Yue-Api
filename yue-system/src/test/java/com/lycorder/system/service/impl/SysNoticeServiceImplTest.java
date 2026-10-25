package com.lycorder.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lycorder.core.exception.BusinessException;
import com.lycorder.system.mapper.SysNoticeMapper;
import com.lycorder.system.model.entity.SysNotice;
import com.lycorder.system.service.SysUserNoticeService;
import com.lycorder.system.service.SysUserService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通知公告服务单元测试。
 *
 * @author LYCoder
 */
class SysNoticeServiceImplTest {

    /**
     * 已发布公告不能直接删除。
     */
    @Test
    void deletePublishedNoticeShouldBeRejected() throws Exception {
        SysUserNoticeService userNoticeService = mock(SysUserNoticeService.class);
        SysUserService userService = mock(SysUserService.class);
        SysNoticeMapper noticeMapper = mock(SysNoticeMapper.class);

        SysNoticeServiceImpl noticeService = new SysNoticeServiceImpl(userNoticeService, userService);
        Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(noticeService, noticeMapper);

        SysNotice notice = new SysNotice();
        notice.setId(1L);
        notice.setPublishStatus(1);
        when(noticeMapper.selectBatchIds(anyCollection())).thenReturn(List.of(notice));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> noticeService.deleteNotices("1")
        );
        assertEquals("已发布的公告请先撤回再删除", exception.getMessage());
        verify(userNoticeService, never()).removeByNoticeIds(List.of(1L));
    }
}
