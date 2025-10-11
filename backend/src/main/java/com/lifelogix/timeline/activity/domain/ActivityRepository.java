package com.lifelogix.timeline.activity.domain;

import com.lifelogix.timeline.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByUserIdOrderByCategory(Long userId);
    boolean existsByCategory(Category category);
    boolean existsByCategoryAndName(Category category, String name);
}