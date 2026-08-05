package io.github.novel.mynovel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * 用户信息 响应DTO
 *
 */
@Data
@Builder
public class UserInfoRespDto {

    /**
     * 昵称
     * */
    @Schema(description = "昵称")
    private String nickName;

    /**
     * 用户头像
     * */
    @Schema(description = "用户头像")
    private String userPhoto;

    /**
     * 用户性别
     * */
    @Schema(description = "用户性别")
    private Integer userSex;

    /**
     * 账户余额
     */
    @Schema(description = "账户余额，单位：屋币")
    private Long accountBalance;

    @Schema(description = "是否是有效VIP")
    private Boolean vip;

    @Schema(description = "VIP到期时间")
    private LocalDateTime vipExpireTime;
}
