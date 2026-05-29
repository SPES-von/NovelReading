package com.novelreading.repository;

import com.novelreading.entity.HotNovel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 热门小说模块配置数据访问层
 * 按位置顺序返回6本热门小说
 */
@Repository
public interface HotNovelRepository extends JpaRepository<HotNovel, Integer> {

    List<HotNovel> findAllByOrderByPositionAsc();
}
