package com.novelreading.repository;

import com.novelreading.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 标签数据访问层
 * 用于筛选页作品主题选项
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByOrderByIdAsc();

    boolean existsByName(String name);
}
