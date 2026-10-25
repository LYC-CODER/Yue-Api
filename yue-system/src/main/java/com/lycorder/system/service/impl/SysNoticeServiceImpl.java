package com.lycorder.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lycorder.core.exception.BusinessException;
import com.lycorder.security.util.SecurityUtils;
import com.lycorder.system.mapper.SysNoticeMapper;
import com.lycorder.system.model.entity.SysNotice;
import com.lycorder.system.model.entity.SysUser;
import com.lycorder.system.model.entity.SysUserNotice;
import com.lycorder.system.model.form.NoticeForm;
import com.lycorder.system.model.query.NoticeQuery;
import com.lycorder.system.model.vo.NoticeDetailVO;
import com.lycorder.system.model.vo.NoticePageVO;
import com.lycorder.system.model.vo.UserNoticePageVO;
import com.lycorder.system.service.SysNoticeService;
import com.lycorder.system.service.SysUserNoticeService;
import com.lycorder.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 通知公告业务实现。
 *
 * <p>发布公告时为每个目标用户写入 sys_user_notice，撤回/删除时清理接收状态。</p>
 *
 * @author LYCoder
 */
@Service
@RequiredArgsConstructor
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice>
        implements SysNoticeService {

    /** 目标类型：全体用户 */
    private static final int TARGET_ALL = 1;

    /** 目标类型：指定用户 */
    private static final int TARGET_SPECIFIED = 2;

    /** 发布状态：未发布 */
    private static final int STATUS_DRAFT = 0;

    /** 发布状态：已发布 */
    private static final int STATUS_PUBLISHED = 1;

    /** 发布状态：已撤回 */
    private static final int STATUS_REVOKED = -1;

    private final SysUserNoticeService sysUserNoticeService;
    private final SysUserService sysUserService;

    @Override
    public Page<NoticePageVO> getNoticePage(NoticeQuery queryParams) {
        return this.baseMapper.getNoticePage(
                new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
                queryParams
        );
    }

    @Override
    public NoticeForm getNoticeFormData(Long id) {
        SysNotice entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("通知公告不存在");
        }
        NoticeForm form = new NoticeForm();
        form.setId(entity.getId());
        form.setTitle(entity.getTitle());
        form.setContent(entity.getContent());
        form.setType(entity.getType());
        form.setLevel(entity.getLevel());
        form.setTargetType(entity.getTargetType());
        form.setTargetUserIds(parseUserIds(entity.getTargetUserIds()));
        return form;
    }

    @Override
    public boolean saveNotice(NoticeForm formData) {
        validateTarget(formData);
        SysNotice entity = toEntity(formData);
        entity.setPublishStatus(STATUS_DRAFT);
        entity.setCreateBy(SecurityUtils.getUserId());
        return this.save(entity);
    }

    @Override
    public boolean updateNotice(Long id, NoticeForm formData) {
        SysNotice old = this.getById(id);
        if (old == null) {
            throw new BusinessException("通知公告不存在");
        }
        if (Objects.equals(old.getPublishStatus(), STATUS_PUBLISHED)) {
            throw new BusinessException("已发布的公告请先撤回再修改");
        }
        validateTarget(formData);

        SysNotice entity = toEntity(formData);
        entity.setId(id);
        entity.setUpdateBy(SecurityUtils.getUserId());
        return this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNotices(String ids) {
        if (StrUtil.isBlank(ids)) {
            throw new BusinessException("删除的通知公告数据为空");
        }
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .map(Long::parseLong)
                .toList();
        // 已发布公告必须先撤回，避免绕过前端直接删除正在生效的通知
        List<SysNotice> notices = this.listByIds(idList);
        boolean hasPublished = notices.stream()
                .anyMatch(notice -> Objects.equals(notice.getPublishStatus(), STATUS_PUBLISHED));
        if (hasPublished) {
            throw new BusinessException("已发布的公告请先撤回再删除");
        }
        boolean result = this.removeByIds(idList);
        if (result) {
            sysUserNoticeService.removeByNoticeIds(idList);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishNotice(Long id) {
        SysNotice notice = this.getById(id);
        if (notice == null) {
            throw new BusinessException("通知公告不存在");
        }
        if (Objects.equals(notice.getPublishStatus(), STATUS_PUBLISHED)) {
            throw new BusinessException("通知公告已发布");
        }

        // 指定用户需要先校验接收人；全体用户改为分批查询，避免一次性加载全部用户
        List<Long> targetUserIds = null;
        if (Objects.equals(notice.getTargetType(), TARGET_SPECIFIED)) {
            targetUserIds = resolveSpecifiedUsers(notice);
            if (CollectionUtil.isEmpty(targetUserIds)) {
                throw new BusinessException("没有可接收通知的用户");
            }
        }

        notice.setPublisherId(SecurityUtils.getUserId());
        notice.setPublishStatus(STATUS_PUBLISHED);
        notice.setPublishTime(LocalDateTime.now());
        notice.setRevokeTime(null);
        notice.setUpdateBy(SecurityUtils.getUserId());
        boolean result = this.updateById(notice);
        if (!result) {
            return false;
        }

        // 重新发布时先清理旧的接收状态，再按最新目标用户生成
        sysUserNoticeService.removeByNoticeIds(List.of(id));
        if (targetUserIds != null) {
            saveUserNotices(id, targetUserIds);
        } else if (createAllUserNotices(id) == 0) {
            throw new BusinessException("没有可接收通知的用户");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean revokeNotice(Long id) {
        SysNotice notice = this.getById(id);
        if (notice == null) {
            throw new BusinessException("通知公告不存在");
        }
        if (!Objects.equals(notice.getPublishStatus(), STATUS_PUBLISHED)) {
            throw new BusinessException("通知公告未发布或已撤回");
        }

        notice.setPublishStatus(STATUS_REVOKED);
        notice.setRevokeTime(LocalDateTime.now());
        notice.setUpdateBy(SecurityUtils.getUserId());
        boolean result = this.updateById(notice);
        if (result) {
            sysUserNoticeService.removeByNoticeIds(List.of(id));
        }
        return result;
    }

    @Override
    public NoticeDetailVO getNoticeDetail(Long id) {
        NoticeDetailVO detail = this.baseMapper.getNoticeDetail(id);
        if (detail == null) {
            throw new BusinessException("通知公告不存在");
        }
        return detail;
    }

    @Override
    public Page<UserNoticePageVO> getMyNoticePage(NoticeQuery queryParams) {
        queryParams.setUserId(SecurityUtils.getUserId());
        return sysUserNoticeService.getMyNoticePage(
                new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
                queryParams
        );
    }

    @Override
    public NoticeDetailVO getMyNoticeDetail(Long id) {
        Long userId = SecurityUtils.getUserId();
        if (!sysUserNoticeService.exists(id, userId)) {
            throw new BusinessException("通知公告不存在或无权查看");
        }
        SysNotice notice = this.getById(id);
        if (notice == null || !Objects.equals(notice.getPublishStatus(), STATUS_PUBLISHED)) {
            throw new BusinessException("通知公告已撤回");
        }
        sysUserNoticeService.markRead(id, userId);
        return this.getNoticeDetail(id);
    }

    @Override
    public void readAll() {
        sysUserNoticeService.readAll(SecurityUtils.getUserId());
    }

    @Override
    public long getUnreadCount() {
        return sysUserNoticeService.countUnread(SecurityUtils.getUserId());
    }

    /**
     * 校验目标类型与指定用户。
     *
     * @param formData 通知表单
     */
    private void validateTarget(NoticeForm formData) {
        if (!Objects.equals(formData.getTargetType(), TARGET_SPECIFIED)) {
            return;
        }
        if (CollectionUtil.isEmpty(formData.getTargetUserIds())) {
            throw new BusinessException("指定用户不能为空");
        }
        String targetUserIds = joinUserIds(formData.getTargetUserIds());
        if (targetUserIds != null && targetUserIds.length() > 255) {
            throw new BusinessException("指定用户数量过多，请减少接收人");
        }
    }

    /**
     * 将表单转换为实体。
     *
     * @param formData 通知表单
     * @return 通知实体
     */
    private SysNotice toEntity(NoticeForm formData) {
        SysNotice entity = new SysNotice();
        entity.setTitle(formData.getTitle());
        entity.setContent(formData.getContent());
        entity.setType(formData.getType());
        entity.setLevel(formData.getLevel());
        entity.setTargetType(formData.getTargetType());
        // 全体用户不保存指定用户 ID；使用空串而不是 null，确保从“指定用户”改成“全体”时能清空旧值
        entity.setTargetUserIds(Objects.equals(formData.getTargetType(), TARGET_SPECIFIED)
                ? joinUserIds(formData.getTargetUserIds())
                : "");
        return entity;
    }

    /**
     * 解析指定用户，自动过滤禁用或已删除账号。
     *
     * @param notice 通知公告
     * @return 可接收通知的用户 ID
     */
    private List<Long> resolveSpecifiedUsers(SysNotice notice) {
        List<Long> ids = parseUserIds(notice.getTargetUserIds());
        if (CollectionUtil.isEmpty(ids)) {
            throw new BusinessException("指定用户不能为空");
        }
        return sysUserService.list(new LambdaQueryWrapper<SysUser>()
                        .select(SysUser::getId)
                        .in(SysUser::getId, ids)
                        .eq(SysUser::getStatus, 1))
                .stream()
                .map(SysUser::getId)
                .toList();
    }

    /**
     * 为全体正常用户分批生成通知记录。
     *
     * @param noticeId 通知 ID
     * @return 实际写入的用户数量
     */
    private int createAllUserNotices(Long noticeId) {
        int pageNum = 1;
        int pageSize = 500;
        int total = 0;
        while (true) {
            Page<SysUser> userPage = sysUserService.page(
                    new Page<>(pageNum, pageSize),
                    new LambdaQueryWrapper<SysUser>()
                            .select(SysUser::getId)
                            .eq(SysUser::getStatus, 1)
            );
            List<SysUser> records = userPage.getRecords();
            if (CollectionUtil.isEmpty(records)) {
                break;
            }
            saveUserNotices(noticeId, records.stream().map(SysUser::getId).toList());
            total += records.size();
            if (records.size() < pageSize) {
                break;
            }
            pageNum++;
        }
        return total;
    }

    /**
     * 批量保存用户通知接收记录。
     *
     * @param noticeId 通知 ID
     * @param userIds  用户 ID 集合
     */
    private void saveUserNotices(Long noticeId, List<Long> userIds) {
        if (CollectionUtil.isEmpty(userIds)) {
            return;
        }
        List<SysUserNotice> userNotices = userIds.stream().map(userId -> {
            SysUserNotice userNotice = new SysUserNotice();
            userNotice.setNoticeId(noticeId);
            userNotice.setUserId(userId);
            userNotice.setIsRead(0);
            return userNotice;
        }).toList();
        sysUserNoticeService.saveBatch(userNotices);
    }

    /**
     * 解析逗号分隔的用户 ID。
     *
     * @param value 用户 ID 字符串
     * @return 用户 ID 列表
     */
    private List<Long> parseUserIds(String value) {
        if (StrUtil.isBlank(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    /**
     * 拼接用户 ID 字符串。
     *
     * @param userIds 用户 ID 列表
     * @return 逗号分隔的用户 ID
     */
    private String joinUserIds(List<Long> userIds) {
        if (CollectionUtil.isEmpty(userIds)) {
            return null;
        }
        return userIds.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
