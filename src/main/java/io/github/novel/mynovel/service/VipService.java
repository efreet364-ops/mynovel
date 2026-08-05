package io.github.novel.mynovel.service;

import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dto.resp.VipProductRespDto;
import java.util.List;

public interface VipService {

    RestResp<List<VipProductRespDto>> listProducts();
}
