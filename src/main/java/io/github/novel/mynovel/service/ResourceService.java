package io.github.novel.mynovel.service;

import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dto.resp.ImgVerifyCodeRespDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ResourceService {

    /**
     * 获取图片验证码
     *
     * @throws IOException 验证码图片生成失败
     * @return Base64编码的图片
     */
    RestResp<ImgVerifyCodeRespDto> getImgVerifyCode() throws IOException;

    /*
    * 上传图片
    *
    * 查询回显，返回图片url
    * */
    RestResp<String> uploadImage(MultipartFile file) throws Exception;
}
