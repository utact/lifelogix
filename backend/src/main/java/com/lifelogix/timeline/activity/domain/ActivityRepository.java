package com.lifelogix.timeline.activity.domain;

import com.lifelogix.timeline.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    @Query("SELECT a FROM Activity a JOIN FETCH a.user JOIN FETCH a.category c LEFT JOIN FETCH c.user WHERE a.user.id = :userId ORDER BY c.name, a.name")
    List<Activity> findByUserIdOrderByCategory(@Param("userId") Long userId);
    boolean existsByCategory(Category category);
    boolean existsByCategoryAndName(Category category, String name);
}