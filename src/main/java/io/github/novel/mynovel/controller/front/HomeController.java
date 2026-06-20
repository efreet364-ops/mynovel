package io.github.novel.mynovel.controller.front;

import io.github.novel.mynovel.core.common.constant.ApiRouterConsts;
import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dto.resp.HomeBookRespDto;
import io.github.novel.mynovel.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页模块 API 接口
 *
 * @author xiongxiaoyang
 * @date 2022/5/12
 */
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_HOME_URL_PREFIX)
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /**
     * 首页小说推荐查询接口
     * */
    @GetMapping("books")
    public RestResp<List<HomeBookRespDto>> listHomeBooks(){
        return homeService.listHomeBooks();
    }
}
