package com.novelreading.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理端评论列表 DTO：评论内容、评论时间、用户ID、审核状态
 */
@Data
public class AdminCommentDTO {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private Long userId;
    private Integer audit;
}
