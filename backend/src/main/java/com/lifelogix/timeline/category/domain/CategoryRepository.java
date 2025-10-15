package com.lifelogix.timeline.category.domain;

import com.lifelogix.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.user LEFT JOIN FETCH c.parent WHERE c.user.id = :userId OR c.user IS NULL")
    List<Category> findByUserIdOrUserIsNull(@Param("userId") Long userId);
    boolean existsByUserAndName(User user, String name);
}