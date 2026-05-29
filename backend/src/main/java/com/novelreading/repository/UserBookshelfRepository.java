package com.novelreading.repository;

import com.novelreading.entity.UserBookshelf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户书架数据访问层
 */
@Repository
public interface UserBookshelfRepository extends JpaRepository<UserBookshelf, Long> {

    Optional<UserBookshelf> findByUser_InternalIdAndNovel_Id(Long userId, Long novelId);

    boolean existsByUser_InternalIdAndNovel_Id(Long userId, Long novelId);

    List<UserBookshelf> findByUser_InternalIdOrderByCreatedAtDesc(Long userId);
}
