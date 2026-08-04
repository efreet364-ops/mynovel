package io.github.novel.mynovel.dao.mapper;

import io.github.novel.mynovel.dao.entity.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 用户信息 Mapper 接口
 * </p>
 *
 * @author efreet233
 * @date 2026/06/19
 */
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    int increaseAccountBalance(@Param("userId") Long userId, @Param("amount") Long amount);
}
