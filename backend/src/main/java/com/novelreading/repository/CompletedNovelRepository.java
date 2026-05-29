package com.novelreading.repository;

import com.novelreading.entity.CompletedNovel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 完本推荐模块配置数据访问层
 * 按位置顺序返回9本完本小说（前3条是经典完本，后6条是完本推荐）
 */
@Repository
public interface CompletedNovelRepository extends JpaRepository<CompletedNovel, Integer> {

    List<CompletedNovel> findAllByOrderByPositionAsc();
}
