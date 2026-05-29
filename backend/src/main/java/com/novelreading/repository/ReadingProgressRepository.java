package com.novelreading.repository;

import com.novelreading.entity.ReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 阅读进度数据访问层
 * 记录和查询用户阅读进度，可配合 Redis 缓存
 */
@Repository
public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {

    Optional<ReadingProgress> findByUser_InternalIdAndNovel_Id(Long userId, Long novelId);

    /** 当前用户所有阅读进度，按最后阅读时间倒序 */
    List<ReadingProgress> findByUser_InternalIdOrderByLastReadAtDesc(Long userId);

    @Query("SELECT COUNT(rp) FROM ReadingProgress rp")
    long countAllReadEvents();

    @Query("SELECT COUNT(DISTINCT rp.user.internalId) FROM ReadingProgress rp")
    long countDistinctReadingUsers();

    @Query("SELECT COUNT(DISTINCT rp.user.internalId) FROM ReadingProgress rp WHERE rp.lastReadAt >= :since")
    long countDistinctActiveUsersSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(rp) FROM ReadingProgress rp WHERE rp.lastReadAt >= :since")
    long countReadEventsSince(@Param("since") LocalDateTime since);

    @Query(value = """
            SELECT DATE(rp.last_read_at) AS day, COUNT(*) AS read_count
            FROM reading_progress rp
            WHERE rp.last_read_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)
            GROUP BY DATE(rp.last_read_at)
            ORDER BY day
            """, nativeQuery = true)
    List<Object[]> findRecent7DayReadTrend();

    @Query(value = """
            SELECT n.id AS novel_id,
                   n.title AS novel_title,
                   n.view_count AS view_count,
                   COUNT(rp.id) AS reading_users
            FROM novel n
            LEFT JOIN reading_progress rp ON rp.novel_id = n.id
            GROUP BY n.id, n.title, n.view_count
            ORDER BY (n.view_count * 0.6 + COUNT(rp.id) * 40) DESC, n.view_count DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Object[]> findTop10NovelHeatStats();

    @Query("SELECT rp FROM ReadingProgress rp JOIN FETCH rp.user u JOIN FETCH rp.novel n ORDER BY rp.lastReadAt DESC")
    List<ReadingProgress> findRecentWithUserAndNovel(Pageable pageable);
}
