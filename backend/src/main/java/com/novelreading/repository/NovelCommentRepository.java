package com.novelreading.repository;

import com.novelreading.entity.NovelComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 小说评论数据访问层
 * 按小说ID查询，审核=1 且 踩击数≤10，按点赞数倒序
 */
@Repository
public interface NovelCommentRepository extends JpaRepository<NovelComment, Long> {

    @Query("SELECT c FROM NovelComment c JOIN FETCH c.user u WHERE c.novel.id = :novelId AND c.audit = 1 AND c.dislikeCount <= 10 ORDER BY c.likeCount DESC, c.createdAt DESC")
    List<NovelComment> findByNovelIdAuditedAndDislikeCountLte10OrderByLikeCountDesc(@Param("novelId") Long novelId);

    /** 管理端：全部评论，按评论时间倒序 */
    @Query("SELECT c FROM NovelComment c JOIN FETCH c.user u ORDER BY c.createdAt DESC")
    List<NovelComment> findAllForAdminOrderByCreatedAtDesc();

    /** 管理端搜索：按关键词（评论内容/用户ID 模糊）和日期筛选，返回评论 ID 列表 */
    @Query(value = "SELECT c.id FROM novel_comment c WHERE " +
        "(:keyword IS NULL OR :keyword = '' OR c.content LIKE CONCAT('%', :keyword, '%') OR CAST(c.user_id AS CHAR) LIKE CONCAT('%', :keyword, '%')) " +
        "AND (:dateStr IS NULL OR :dateStr = '' OR DATE(c.created_at) = :dateStr) " +
        "ORDER BY c.created_at DESC", nativeQuery = true)
    List<Long> findCommentIdsForAdminSearch(@Param("keyword") String keyword, @Param("dateStr") String dateStr);

    /** 按 ID 列表查询并带 user，保持创建时间倒序 */
    @Query("SELECT c FROM NovelComment c JOIN FETCH c.user u WHERE c.id IN :ids ORDER BY c.createdAt DESC")
    List<NovelComment> findByIdInWithUserOrderByCreatedAtDesc(@Param("ids") List<Long> ids);

    @Query("SELECT COUNT(c) FROM NovelComment c")
    long countAllComments();

    @Query("SELECT COUNT(c) FROM NovelComment c WHERE c.createdAt >= :since")
    long countCommentsSince(@Param("since") LocalDateTime since);

    @Query("SELECT c FROM NovelComment c JOIN FETCH c.user u JOIN FETCH c.novel n ORDER BY c.createdAt DESC")
    List<NovelComment> findRecentWithUserAndNovel(Pageable pageable);
}
