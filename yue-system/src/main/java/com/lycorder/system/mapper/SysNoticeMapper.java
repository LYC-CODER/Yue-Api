package com.lycorder.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lycorder.system.model.entity.SysNotice;
import com.lycorder.system.model.query.NoticeQuery;
import com.lycorder.system.model.vo.NoticeDetailVO;
import com.lycorder.system.model.vo.NoticePageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 通知公告 Mapper。
 *
 * @author LYCoder
 */
@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {

    /**
     * 管理端通知分页。
     *
     * @param page        分页参数
     * @param queryParams 查询条件
     * @return 通知分页数据
     */
    Page<NoticePageVO> getNoticePage(Page<NoticePageVO> page, @Param("queryParams") NoticeQuery queryParams);

    /**
     * 查询通知详情。
     *
     * @param id 通知 ID
     * @return 通知详情
     */
    NoticeDetailVO getNoticeDetail(@Param("id") Long id);
}
