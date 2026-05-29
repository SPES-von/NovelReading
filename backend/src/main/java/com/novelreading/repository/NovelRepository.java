package com.novelreading.repository;

import com.novelreading.entity.Novel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 小说数据访问层
 * 支持按文库、完本、特色推荐、热度等条件查询
 */
@Repository
public interface NovelRepository extends JpaRepository<Novel, Long>, JpaSpecificationExecutor<Novel> {

    /** 按热度(阅读量)降序，用于强推榜/热点榜 */
    List<Novel> findTop10ByOrderByViewCountDesc();

    /** 按推荐数降序，用于新书榜 */
    List<Novel> findTop10ByOrderByRecommendCountDesc();

    /** 排行榜：ORDER BY 取前10条 */
    List<Novel> findTop10ByOrderByWordCountDesc();
    List<Novel> findTop10ByOrderByRatingDesc();
    List<Novel> findTop10ByOrderByFlowerCountDesc();
    List<Novel> findTop10ByOrderByFavoriteCountDesc();

    /** 新书精选：按 new_select_sort 升序 */
    List<Novel> findByNewSelectSortGreaterThanOrderByNewSelectSortAsc(int minSort);

    /** 特色推荐小说 */
    List<Novel> findByIsFeaturedTrue();

    /** 完本推荐 */
    List<Novel> findByIsCompletedTrueOrderByViewCountDesc(Pageable pageable);

    /** 全书页榜单：仅完本(is_completed=1)，各取前15 */
    List<Novel> findTop15ByIsCompletedTrueOrderByFlowerCountDesc();
    List<Novel> findTop15ByIsCompletedTrueOrderByRatingDesc();
    List<Novel> findTop15ByIsCompletedTrueOrderByViewCountDesc();
    List<Novel> findTop15ByIsCompletedTrueOrderByFavoriteCountDesc();

    /** 按文库查询 */
    List<Novel> findByPublisherIdOrderByViewCountDesc(Long publisherId, Pageable pageable);

    /** 搜索：标题模糊查询 */
    Page<Novel> findByTitleContaining(String keyword, Pageable pageable);

    /** 搜索内容：标题、简介、标签名模糊查询，分页返回当前页列表 */
    @Query("SELECT n FROM Novel n WHERE n.id IN (" +
           "SELECT DISTINCT n2.id FROM Novel n2 LEFT JOIN n2.tags t " +
           "WHERE n2.title LIKE CONCAT('%', :kw, '%') " +
           "OR (n2.synopsis IS NOT NULL AND n2.synopsis LIKE CONCAT('%', :kw, '%')) " +
           "OR t.name LIKE CONCAT('%', :kw, '%')) ORDER BY n.id")
    List<Novel> searchByKeywordContent(@Param("kw") String keyword, Pageable pageable);

    /** 搜索总数：用原生 SQL 统计匹配的 distinct 小说数，避免 JPQL 子查询 count 结果不准 */
    @Query(value = "SELECT COUNT(*) FROM (" +
           "SELECT DISTINCT n2.id FROM novel n2 " +
           "LEFT JOIN novel_tag nt ON n2.id = nt.novel_id " +
           "LEFT JOIN tag t ON nt.tag_id = t.id " +
           "WHERE n2.title LIKE CONCAT('%', :kw, '%') " +
           "OR (n2.synopsis IS NOT NULL AND n2.synopsis LIKE CONCAT('%', :kw, '%')) " +
           "OR t.name LIKE CONCAT('%', :kw, '%')) AS sub",
           nativeQuery = true)
    Long countSearchByKeyword(@Param("kw") String keyword);
}
