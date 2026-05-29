package com.novelreading.repository;

import com.novelreading.entity.StrongRecommendChart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 强推榜配置数据访问层
 */
@Repository
public interface StrongRecommendChartRepository extends JpaRepository<StrongRecommendChart, Integer> {

    List<StrongRecommendChart> findAllByOrderByPositionAsc();
}
