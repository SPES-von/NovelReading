package com.novelreading.service;

import com.novelreading.dto.AdminCommentDTO;
import com.novelreading.dto.NovelCommentDTO;
import com.novelreading.entity.Novel;
import com.novelreading.entity.NovelComment;
import com.novelreading.entity.User;
import com.novelreading.repository.NovelCommentRepository;
import com.novelreading.repository.NovelRepository;
import com.novelreading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 小说评论服务
 * 按书籍ID查询评论（踩击数≤10，按点赞数倒序）；发布评论需登录
 */
@Service
@RequiredArgsConstructor
public class NovelCommentService {

    private final NovelCommentRepository novelCommentRepository;
    private final NovelRepository novelRepository;
    private final UserRepository userRepository;

    /** 获取某本书的可见评论：审核=1 且 踩击数≤10，按点赞数降序 */
    public List<NovelCommentDTO> listByNovelId(Long novelId) {
        List<NovelComment> list = novelCommentRepository.findByNovelIdAuditedAndDislikeCountLte10OrderByLikeCountDesc(novelId);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** 发布评论：需已登录用户，写入 novel_comment 表 */
    @Transactional
    public NovelCommentDTO create(Long userId, Long novelId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
        User user = userRepository.findByInternalId(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) {
            throw new IllegalArgumentException("书籍不存在");
        }
        NovelComment comment = new NovelComment();
        comment.setUser(user);
        comment.setNovel(novel);
        comment.setContent(content.trim());
        comment.setLikeCount(0);
        comment.setDislikeCount(0);
        comment.setAudit(0); /* 发布时默认为 0，审核通过后改为 1 才显示 */
        comment = novelCommentRepository.save(comment);
        return toDTO(comment);
    }

    /** 点赞：评论 like_count +1，需校验评论属于该书 */
    @Transactional
    public void incrementLike(Long novelId, Long commentId) {
        NovelComment comment = novelCommentRepository.findById(commentId).orElse(null);
        if (comment == null || !comment.getNovel().getId().equals(novelId)) {
            throw new IllegalArgumentException("评论不存在或不属于该书籍");
        }
        comment.setLikeCount((comment.getLikeCount() == null ? 0 : comment.getLikeCount()) + 1);
        novelCommentRepository.save(comment);
    }

    /** 踩：评论 dislike_count +1，需校验评论属于该书 */
    @Transactional
    public void incrementDislike(Long novelId, Long commentId) {
        NovelComment comment = novelCommentRepository.findById(commentId).orElse(null);
        if (comment == null || !comment.getNovel().getId().equals(novelId)) {
            throw new IllegalArgumentException("评论不存在或不属于该书籍");
        }
        comment.setDislikeCount((comment.getDislikeCount() == null ? 0 : comment.getDislikeCount()) + 1);
        novelCommentRepository.save(comment);
    }

    /** 管理端：获取全部评论，按评论时间倒序；可选关键词（评论内容/用户ID 模糊）和日期筛选 */
    public List<AdminCommentDTO> listAllForAdmin(String keyword, Integer year, Integer month, Integer day) {
        String dateStr = null;
        if (year != null && month != null && day != null) {
            dateStr = LocalDate.of(year, month, day).toString();
        }
        boolean hasFilter = (keyword != null && !keyword.isBlank()) || (dateStr != null && !dateStr.isEmpty());
        if (!hasFilter) {
            List<NovelComment> list = novelCommentRepository.findAllForAdminOrderByCreatedAtDesc();
            return list.stream().map(this::toAdminDTO).collect(Collectors.toList());
        }
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        List<Long> ids = novelCommentRepository.findCommentIdsForAdminSearch(kw, dateStr);
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        List<NovelComment> list = novelCommentRepository.findByIdInWithUserOrderByCreatedAtDesc(ids);
        return list.stream().map(this::toAdminDTO).collect(Collectors.toList());
    }

    /** 管理端：审核通过，将 audit 设为 1 */
    @Transactional
    public boolean audit(Long commentId) {
        NovelComment comment = novelCommentRepository.findById(commentId).orElse(null);
        if (comment == null) return false;
        comment.setAudit(1);
        novelCommentRepository.save(comment);
        return true;
    }

    /** 管理端：删除评论 */
    @Transactional
    public boolean deleteById(Long commentId) {
        if (!novelCommentRepository.existsById(commentId)) return false;
        novelCommentRepository.deleteById(commentId);
        return true;
    }

    private AdminCommentDTO toAdminDTO(NovelComment c) {
        AdminCommentDTO dto = new AdminCommentDTO();
        dto.setId(c.getId());
        dto.setContent(c.getContent());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUserId(c.getUser().getId());
        dto.setAudit(c.getAudit() != null ? c.getAudit() : 0);
        return dto;
    }

    private NovelCommentDTO toDTO(NovelComment c) {
        NovelCommentDTO dto = new NovelCommentDTO();
        dto.setId(c.getId());
        dto.setUserId(c.getUser().getId());
        dto.setNovelId(c.getNovel().getId());
        dto.setContent(c.getContent());
        dto.setLikeCount(c.getLikeCount());
        dto.setDislikeCount(c.getDislikeCount());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUserNickname(c.getUser().getNickname() != null ? c.getUser().getNickname() : c.getUser().getUsername());
        dto.setUserAvatarUrl(c.getUser().getAvatarUrl());
        return dto;
    }
}
