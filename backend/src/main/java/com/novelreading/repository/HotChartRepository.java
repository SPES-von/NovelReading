package com.novelreading.repository;

import com.novelreading.entity.HotChart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 热点榜配置数据访问层
 */
@Repository
public interface HotChartRepository extends JpaRepository<HotChart, Integer> {

    List<HotChart> findAllByOrderByPositionAsc();
}
