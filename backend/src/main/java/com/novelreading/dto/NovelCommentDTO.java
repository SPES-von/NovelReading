package com.novelreading.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论展示 DTO：含用户昵称、头像，供前端「热门评论」列表展示
 */
@Data
public class NovelCommentDTO {
    private Long id;
    private Long userId;
    private Long novelId;
    private String content;
    private Integer likeCount;
    private Integer dislikeCount;
    private LocalDateTime createdAt;
    private String userNickname;
    private String userAvatarUrl;
}
