package io.github.novel.mynovel.service.impl;

import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dao.entity.AuthorInfo;
import io.github.novel.mynovel.dao.mapper.AuthorInfoMapper;
import io.github.novel.mynovel.dto.req.AuthorRegisterReqDto;
import io.github.novel.mynovel.dto.AuthorInfoDto;
import io.github.novel.mynovel.manager.cache.AuthorInfoCacheManager;
import io.github.novel.mynovel.service.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorInfoCacheManager authorInfoCacheManager;

    private final AuthorInfoMapper authorInfoMapper;

    @Override
    public RestResp<Void> register(AuthorRegisterReqDto dto) {
        // 校验该用户是否已经是作家
        AuthorInfoDto author = authorInfoCacheManager.getAuthor(dto.getUserId());
        if (Objects.nonNull(author)) {
            // 该用户注册过作家，直接返回
            return RestResp.ok();
        }

        AuthorInfo authorInfo = AuthorInfo.builder()
                .userId(dto.getUserId())
                .penName(dto.getPenName())
                .telPhone(dto.getTelPhone())
                .email(dto.getEmail())
                .inviteCode("0")
                .chatAccount(dto.getChatAccount())
                .workDirection(dto.getWorkDirection())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        authorInfoMapper.insert(authorInfo);

        authorInfoCacheManager.evictAuthorCache();
        return RestResp.ok();
    }

    @Override
    public RestResp<Integer> getStatus(Long userId) {
        AuthorInfoDto author = authorInfoCacheManager.getAuthor(userId);
        return Objects.isNull(author) ? RestResp.ok(null) : RestResp.ok(author.getStatus());
    }
}
