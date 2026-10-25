package com.lycorder.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lycorder.system.model.entity.SysNotice;
import com.lycorder.system.model.form.NoticeForm;
import com.lycorder.system.model.query.NoticeQuery;
import com.lycorder.system.model.vo.NoticeDetailVO;
import com.lycorder.system.model.vo.NoticePageVO;
import com.lycorder.system.model.vo.UserNoticePageVO;

/**
 * 通知公告业务接口。
 *
 * @author LYCoder
 */
public interface SysNoticeService extends IService<SysNotice> {

    /** 管理端通知分页 */
    Page<NoticePageVO> getNoticePage(NoticeQuery queryParams);

    /** 获取编辑表单 */
    NoticeForm getNoticeFormData(Long id);

    /** 新增通知 */
    boolean saveNotice(NoticeForm formData);

    /** 修改通知 */
    boolean updateNotice(Long id, NoticeForm formData);

    /** 删除通知 */
    boolean deleteNotices(String ids);

    /** 发布通知 */
    boolean publishNotice(Long id);

    /** 撤回通知 */
    boolean revokeNotice(Long id);

    /** 管理端查看详情 */
    NoticeDetailVO getNoticeDetail(Long id);

    /** 我的通知分页 */
    Page<UserNoticePageVO> getMyNoticePage(NoticeQuery queryParams);

    /** 阅读我的通知详情并标记已读 */
    NoticeDetailVO getMyNoticeDetail(Long id);

    /** 全部标记已读 */
    void readAll();

    /** 当前用户未读数量 */
    long getUnreadCount();
}
