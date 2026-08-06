package io.github.novel.mynovel.dto.resp;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiChatSessionRespDto {

    private String conversationId;

    private String title;

    private String lastMessage;

    private Integer messageCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
