package com.novelreading.repository;

import com.novelreading.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文库数据访问层
 * 提供文库列表查询，用于首页分类下拉菜单
 */
@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    List<Publisher> findAllByOrderBySortOrderAsc();
}
