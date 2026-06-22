package io.github.novel.mynovel.dao.mapper;

import io.github.novel.mynovel.dao.entity.NewsInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.novel.mynovel.dto.resp.NewsInfoRespDto;

import java.util.List;

/**
 * <p>
 * 新闻信息 Mapper 接口
 * </p>
 *
 * @author efreet233
 * @date 2026/06/19
 */
public interface NewsInfoMapper extends BaseMapper<NewsInfo> {

    List<NewsInfoRespDto> selectNewsList();
}
