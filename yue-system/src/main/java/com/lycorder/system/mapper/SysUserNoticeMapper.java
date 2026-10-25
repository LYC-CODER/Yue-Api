package com.lycorder.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lycorder.system.model.entity.SysUserNotice;
import com.lycorder.system.model.query.NoticeQuery;
import com.lycorder.system.model.vo.UserNoticePageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户通知状态 Mapper。
 *
 * @author LYCoder
 */
@Mapper
public interface SysUserNoticeMapper extends BaseMapper<SysUserNotice> {

    /**
     * 当前登录用户的通知分页。
     *
     * @param page        分页参数
     * @param queryParams 查询条件（userId 必填）
     * @return 我的通知分页数据
     */
    Page<UserNoticePageVO> getMyNoticePage(Page<UserNoticePageVO> page,
                                           @Param("queryParams") NoticeQuery queryParams);
}
