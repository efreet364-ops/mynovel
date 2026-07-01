package io.github.novel.mynovel.dao.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.novel.mynovel.dao.entity.BookInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.novel.mynovel.dto.req.BookSearchReqDto;
import io.github.novel.mynovel.dto.resp.BookInfoRespDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 小说信息 Mapper 接口
 * </p>
 *
 * @author efreet233
 * @date 2026/06/19
 */
public interface BookInfoMapper extends BaseMapper<BookInfo> {

    /**
     * 小说搜索
     * @param page mybatis-plus 分页对象
     * @param condition 搜索条件
     * @return 返回结果
     * */
    List<BookInfo> searchBooks(Page<BookInfoRespDto> page, @Param("condition") BookSearchReqDto condition);


}
