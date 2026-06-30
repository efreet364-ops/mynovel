package io.github.novel.mynovel.service;

import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dto.req.AuthorRegisterReqDto;
import io.github.novel.mynovel.dto.resp.UserRegisterRespDto;
import jakarta.validation.Valid;

public interface AuthorService {
    /*
    * 作家注册
    * */
    RestResp<Void> register(@Valid AuthorRegisterReqDto dto);

    /**
     * 查询作家状态
     *
     * @param userId 用户ID
     * @return 作家状态
     */
    RestResp<Integer> getStatus(Long userId);
}
