package com.novelreading.repository;

import com.novelreading.entity.User;
import com.novelreading.entity.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 * 主键为 UserId (username, email)
 */
@Repository
public interface UserRepository extends JpaRepository<User, UserId> {

    Optional<User> findByPk_Username(String username);

    Optional<User> findFirstByPk_Email(String email);

    boolean existsByPk_Username(String username);

    boolean existsByPk_Email(String email);

    @Query("SELECT u FROM User u WHERE u.internalId = :id")
    Optional<User> findByInternalId(@Param("id") Long id);

    /** 管理端搜索：按关键词（用户ID/用户名/邮箱 模糊）和注册日期筛选，返回用户 id 列表 */
    @Query(value = "SELECT id FROM user WHERE " +
        "(:keyword IS NULL OR :keyword = '' OR CAST(id AS CHAR) LIKE CONCAT('%', :keyword, '%') OR username LIKE CONCAT('%', :keyword, '%') OR email LIKE CONCAT('%', :keyword, '%')) " +
        "AND (:dateStr IS NULL OR :dateStr = '' OR DATE(created_at) = :dateStr) " +
        "ORDER BY created_at DESC", nativeQuery = true)
    List<Long> findUserIdsForAdminSearch(@Param("keyword") String keyword, @Param("dateStr") String dateStr);

    @Query("SELECT u FROM User u WHERE u.internalId IN :ids ORDER BY u.createdAt DESC")
    List<User> findByInternalIdInOrderByCreatedAtDesc(@Param("ids") List<Long> ids);

    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    long countCreatedSince(@Param("since") java.time.LocalDateTime since);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isBanned = 1")
    long countBannedUsers();

    List<User> findByOrderByCreatedAtDesc(Pageable pageable);
}
