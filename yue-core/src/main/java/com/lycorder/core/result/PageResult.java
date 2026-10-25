package com.lycorder.core.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页响应结构体
 * <p>
 * 分页场景返回 data + page，列表场景仅返回 data；page 为 null 时不参与序列化。
 *
 * @author LYCoder
 */
@Data
public class PageResult<T> implements Serializable {

    private String code;

    private String msg;

    private List<T> data;

    /**
     * 分页元信息；非分页接口不显示此字段
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Page page;

    /**
     * 构建分页结果（MyBatis-Plus {@link IPage}）。
     *
     * <p>data 为当前页记录列表；page 提供分页元信息。</p>
     *
     * @param page MyBatis-Plus 分页对象，可为 null
     * @param <T>  记录类型
     * @return 分页响应
     */
    public static <T> PageResult<T> success(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());

        // 分页对象或记录为空时返回空列表，避免前端拿到 null
        List<T> records =
                (page == null || page.getRecords() == null)
                        ? Collections.emptyList()
                        : page.getRecords();
        result.setData(records);

        // 分页对象为空时使用默认页码 1 与零值统计
        Page pageMeta = new Page();
        pageMeta.setPageNum(page != null ? page.getCurrent() : 1L);
        pageMeta.setPageSize(page != null ? page.getSize() : 0L);
        pageMeta.setTotal(page != null ? page.getTotal() : 0L);
        result.setPage(pageMeta);

        return result;
    }

    /**
     * 构建列表结果（无分页）。
     *
     * <p>page 置为 null，用于与分页返回区分。</p>
     *
     * @param list 列表数据，可为 null
     * @param <T>  记录类型
     * @return 列表响应，page 为 null
     */
    public static <T> PageResult<T> success(List<T> list) {
        PageResult<T> result = new PageResult<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setData(list != null ? list : Collections.emptyList());
        result.setPage(null);
        return result;
    }

    /**
     * 分页元信息（页码、每页条数、总记录数）。
     */
    @Data
    public static class Page {

        private long pageNum;

        private long pageSize;

        private long total;
    }

}
