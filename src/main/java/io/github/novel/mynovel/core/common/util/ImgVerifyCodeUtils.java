package io.github.novel.mynovel.core.common.util;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.captcha.generator.RandomGenerator;
import lombok.experimental.UtilityClass;

import java.awt.*;
import java.io.IOException;


/**
 * 图片验证码工具类
 *
 *
 *
 */
@UtilityClass
public class ImgVerifyCodeUtils {

    /**
     * 随机产生只有数字的字符串
     */
    private final String randNumber = "0123456789";

    /**
     * 图片宽
     */
    private final int width = 100;

    /**
     * 图片高
     */
    private final int height = 38;


    /**
     * 获得字体
     */
    private Font getFont() {
        return new Font("Fixed", Font.PLAIN, 23);
    }

    /**
     * 自定义验证码生成类
     */
    private class FixedCodeGenerator implements CodeGenerator {

        private final String code;

        public FixedCodeGenerator(String code) {
            this.code = code;
        }

        @Override
        public String generate() {
            return code;
        }

        @Override
        public boolean verify(String code, String userInputCode) {
            return this.code.equals(userInputCode);
        }
    }

    /**
     * 生成校验码图片
     */
    public String genVerifyCodeImg(String verifyCode) throws IOException {
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(width, height);
        lineCaptcha.setGenerator(new FixedCodeGenerator(verifyCode));
        lineCaptcha.setFont(getFont());
        lineCaptcha.createCode();
        return lineCaptcha.getImageBase64();
    }


    /**
     * 获取随机的校验码
     */
    public String getRandomVerifyCode(int num) {
        RandomGenerator randomGenerator = new RandomGenerator(randNumber, num);
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(width, height);
        lineCaptcha.setGenerator(randomGenerator);
        return lineCaptcha.getCode();
    }

}

