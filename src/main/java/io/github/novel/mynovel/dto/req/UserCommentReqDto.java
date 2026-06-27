package io.github.novel.mynovel.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 用户发表评论 请求DTO
 */
@Data
public class UserCommentReqDto {

    private Long userId;

    @Schema(description = "小说ID")
    @NotNull(message="小说ID不能为空！")
    private Long bookId;

    @Schema(description = "评论内容")
    @NotBlank(message="评论不能为空！")
    @Length(min = 10,max = 512)
    private String commentContent;

}
