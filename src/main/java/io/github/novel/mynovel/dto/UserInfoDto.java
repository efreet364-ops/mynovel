package io.github.novel.mynovel.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户信息 DTO
 * 保存进redis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Integer status;

}
