package io.github.novel.mynovel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dao.entity.VipProduct;
import io.github.novel.mynovel.dao.mapper.VipProductMapper;
import io.github.novel.mynovel.dto.resp.VipProductRespDto;
import io.github.novel.mynovel.service.VipService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VipServiceImpl implements VipService {

    private static final int STATUS_ENABLED = 0;

    private final VipProductMapper vipProductMapper;

    @Override
    public RestResp<List<VipProductRespDto>> listProducts() {
        LambdaQueryWrapper<VipProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VipProduct::getStatus, STATUS_ENABLED)
                .orderByAsc(VipProduct::getSort)
                .orderByAsc(VipProduct::getId);
        return RestResp.ok(vipProductMapper.selectList(queryWrapper).stream()
                .map(product -> VipProductRespDto.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .durationDays(product.getDurationDays())
                        .priceCent(product.getPriceCent())
                        .priceCny(product.getPriceCent() / 100)
                        .build())
                .toList());
    }
}
