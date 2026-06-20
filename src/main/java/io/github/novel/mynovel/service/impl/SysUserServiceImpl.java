package io.github.novel.mynovel.service.impl;

import io.github.novel.mynovel.dao.entity.SysUser;
import io.github.novel.mynovel.dao.mapper.SysUserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.novel.mynovel.service.UserService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统用户 服务实现类
 * </p>
 *
 * @author efreet233
 * @since 2026/06/19
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

}
