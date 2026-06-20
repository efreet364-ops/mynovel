package io.github.novel.mynovel;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.lang.Console;
import org.junit.jupiter.api.Test;

import java.sql.SQLOutput;

public class CodeTest {

    @Test
    void gencode() {
        // 自定义纯数字的验证码（随机4位数字，可重复）
        RandomGenerator randomGenerator = new RandomGenerator("0123456789", 4);
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(100, 38);
        lineCaptcha.setGenerator(randomGenerator);


        System.out.println(lineCaptcha.getImageBase64());
        System.out.println(lineCaptcha.verify("1234"));
    }
}
