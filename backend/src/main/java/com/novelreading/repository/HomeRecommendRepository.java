package com.novelreading.repository;

import com.novelreading.entity.HomeRecommend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 首页推荐榜配置数据访问层
 * 按位置顺序返回6本推荐书籍
 */
@Repository
public interface HomeRecommendRepository extends JpaRepository<HomeRecommend, Integer> {

    List<HomeRecommend> findAllByOrderByPositionAsc();
}
