package com.novelreading.repository;

import com.novelreading.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 章节数据访问层
 * 用于阅读页面的章节列表和内容加载
 * 约定：章节顺序优先按 volumeSort（卷序）再按 sortOrder（章序）
 */
@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    /** 读取单章并校验所属小说，适合需要同时校验 novelId 的场景 */
    Optional<Chapter> findByIdAndNovel_Id(Long id, Long novelId);

    /** 旧排序字段：按 volume 字段再按章序排序（保留兼容） */
    List<Chapter> findByNovelIdOrderByVolumeAscSortOrderAsc(Long novelId);

    /** 按卷排序字段升序、再按章节 sort_order 升序，用于详情页选卷与目录顺序 */
    List<Chapter> findByNovelIdOrderByVolumeSortAscSortOrderAsc(Long novelId);

    /** 统计某书章节数（用于详情页章节数展示） */
    long countByNovelId(Long novelId);

    /** 查询指定书+卷的最大章序，便于新增章节时自动计算下一个 sortOrder */
    @Query("SELECT COALESCE(MAX(c.sortOrder), -1) FROM Chapter c WHERE c.novel.id = :novelId AND c.volume = :volume")
    Integer findMaxSortOrderByNovelIdAndVolume(@Param("novelId") Long novelId, @Param("volume") String volume);

    /** 查询指定书的最大卷序（volumeSort），用于新卷插入排序 */
    @Query("SELECT COALESCE(MAX(c.volumeSort), 0) FROM Chapter c WHERE c.novel.id = :novelId")
    Integer findMaxVolumeSortByNovelId(@Param("novelId") Long novelId);

    /** 查询指定书+卷的卷序（volumeSort），用于同卷新增章节沿用卷序 */
    @Query("SELECT COALESCE(MAX(c.volumeSort), 0) FROM Chapter c WHERE c.novel.id = :novelId AND c.volume = :volume")
    Integer findVolumeSortByNovelIdAndVolume(@Param("novelId") Long novelId, @Param("volume") String volume);
}
