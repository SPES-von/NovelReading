package com.novelreading.repository;

import com.novelreading.entity.NewBookChart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 新书榜配置数据访问层
 */
@Repository
public interface NewBookChartRepository extends JpaRepository<NewBookChart, Integer> {

    List<NewBookChart> findAllByOrderByPositionAsc();
}
