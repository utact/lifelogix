package com.lifelogix.timeline.category.domain;

import com.lifelogix.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserIdOrUserIsNull(Long userId);
    boolean existsByUserAndName(User user, String name);
}