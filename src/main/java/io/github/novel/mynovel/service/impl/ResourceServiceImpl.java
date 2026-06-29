package io.github.novel.mynovel.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.core.util.AliyunOSSOperator;
import io.github.novel.mynovel.dto.resp.ImgVerifyCodeRespDto;
import io.github.novel.mynovel.manager.VerifyCodeManager;
import io.github.novel.mynovel.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final VerifyCodeManager verifyCodeManager;

    private final AliyunOSSOperator aliyunOSSOperator;

    @Override
    public RestResp<ImgVerifyCodeRespDto> getImgVerifyCode() throws IOException {
        String sessionId = IdWorker.get32UUID();
        return RestResp.ok(ImgVerifyCodeRespDto.builder()
                .sessionId(sessionId)
                .img(verifyCodeManager.genImgVerifyCode(sessionId))
                .build()
        );
    }

    @Override
    public RestResp<String> uploadImage(MultipartFile file) throws Exception {
        if (!file.isEmpty()) {
            String url = aliyunOSSOperator.upload(file.getBytes(), file.getOriginalFilename());
            return RestResp.ok(url);
        }
        return null;
    }


}
