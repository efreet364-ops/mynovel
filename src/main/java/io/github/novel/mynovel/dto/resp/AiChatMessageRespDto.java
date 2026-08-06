package io.github.novel.mynovel.dto.resp;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiChatMessageRespDto {

    private Long id;

    private String role;

    private String content;

    private LocalDateTime createTime;
}
